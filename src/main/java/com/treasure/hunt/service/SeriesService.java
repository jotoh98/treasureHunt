package com.treasure.hunt.service;

import com.esotericsoftware.kryo.Kryo;
import com.treasure.hunt.analysis.StatisticObject;
import com.treasure.hunt.analysis.StatisticsWithId;
import com.treasure.hunt.analysis.StatisticsWithIdsAndPath;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.io.FileService;
import com.treasure.hunt.utils.EventBusUtils;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public class SeriesService {
    public static final String STATS_FILE_NAME = "stats.huntstats";
    public static final String HUNT_FILE_EXTENSION = ".hunt";
    private static SeriesService instance;

    private final Kryo kryo;
    private final FileChooser fileChooser;

    private SeriesService() {
        fileChooser = new FileChooser();
        fileChooser.setInitialFileName("saved.hunts");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("hunt series files (*.hunts)", "*.hunts");
        fileChooser.getExtensionFilters().add(extFilter);

        kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
    }


    public static SeriesService getInstance() {
        if (SeriesService.instance == null) {
            SeriesService.instance = new SeriesService();
        }
        return SeriesService.instance;
    }

    @SneakyThrows
    public void runSeries(Integer rounds, GameManager gameManager, Consumer<Double> progressConsumer) {
        AtomicReference<File> selectedFile = new AtomicReference<>();
        CountDownLatch userSelectedFileLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            selectedFile.set(fileChooser.showSaveDialog(new Stage()));
            userSelectedFileLatch.countDown();
        });
        userSelectedFileLatch.await();
        if (selectedFile.get() == null) {
            return;
        }
        runSeriesAndSaveToFile(rounds, gameManager, progressConsumer, selectedFile.get());
    }

    /**
     * @param rounds           amount of runs
     * @param gameManager      gameManager to be copied (preserves state for multiple starts with same states)
     * @param progressConsumer consumer for working progress, We have 4 workload points per run 1 for copying GameManager, 2 for the actual run and one for writing the file
     * @param selectedFile     the file the runs are written to
     */
    public void runSeriesAndSaveToFile(Integer rounds, GameManager gameManager, Consumer<Double> progressConsumer, File selectedFile) {
        int totalWorkLoad = rounds * 4;
        AtomicInteger workLoadDone = new AtomicInteger();
        List<GameManager> gameManagerList = new ArrayList<>(rounds);
        log.debug("Duplicating game manager");
        for (int i = 0; i < rounds; i++) {
            GameManager gameMangerCopy = kryo.copy(gameManager);
            gameMangerCopy.init();
            gameManagerList.add(gameMangerCopy);
            progressConsumer.accept(workLoadDone.incrementAndGet() / (double) totalWorkLoad);
        }

        log.debug("Running series");
        List<CompletableFuture<Void>> runFutures = new ArrayList<>(rounds);
        for (GameManager managerInstance : gameManagerList) {
            CompletableFuture<Void> beat = managerInstance.beat();
            beat.thenRun(() -> {
                workLoadDone.addAndGet(2);
                progressConsumer.accept(workLoadDone.get() / (double) totalWorkLoad);
            });
            runFutures.add(beat);
        }

        CompletableFuture<Void> allRunsFinished = CompletableFuture.allOf(runFutures.toArray(new CompletableFuture[runFutures.size()]));

        allRunsFinished.join();

        log.debug("Writing runs to file");
        saveAllInstancesToZip(gameManagerList, selectedFile, progressConsumer, workLoadDone.get(), totalWorkLoad);
    }

    @SneakyThrows
    private void saveAllInstancesToZip(List<GameManager> gameManagerList, File selectedFile, Consumer<Double> progressConsumer, int workLoadDone, int totalWorkLoad) {
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(selectedFile));
        writeStatisticsFile(gameManagerList, zipOutputStream);
        writeGameMangers(gameManagerList, progressConsumer, workLoadDone, totalWorkLoad, zipOutputStream);
        zipOutputStream.close();
    }

    private void writeStatisticsFile(List<GameManager> gameManagerList, ZipOutputStream zipOutputStream) throws IOException {
        List<StatisticsWithId> statisticsWithIds = gameManagerList.stream()
                .map(gameManager -> {
                    List<StatisticObject> statisticObjects = gameManager.getStatistics().get();
                    return new StatisticsWithId(gameManagerList.indexOf(gameManager), new ArrayList<>(statisticObjects));
                })
                .collect(Collectors.toList());
        zipOutputStream.putNextEntry(new ZipEntry(STATS_FILE_NAME));
        FileService.getInstance()
                .writeStatisticsWithId(new ArrayList<>(statisticsWithIds), zipOutputStream);
    }

    private void writeGameMangers(List<GameManager> gameManagerList, Consumer<Double> progressConsumer, int workLoadDone, double totalWorkLoad, ZipOutputStream zipOutputStream) throws IOException {
        for (GameManager gameManager : gameManagerList) {
            zipOutputStream.putNextEntry(new ZipEntry(gameManagerList.indexOf(gameManager) + HUNT_FILE_EXTENSION));
            FileService.getInstance().writeGameDataToOutputStream(gameManager, zipOutputStream);
            progressConsumer.accept(++workLoadDone / totalWorkLoad);
        }
    }

    public void readStatistics() {
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile == null) {
            return;
        }
        readStatistics(selectedFile.toPath());
    }

    @SneakyThrows
    public void readStatistics(Path path) {
        ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(path));
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.getName().equals(STATS_FILE_NAME)) {
                readStatisticFile(zipInputStream, path);
                return;
            }
        }
        throw new FileNotFoundException(String.format("No %s file found in %s", STATS_FILE_NAME, path.toString()));
    }

    private void readStatisticFile(InputStream inputStream, Path path) {
        CompletableFuture.runAsync(() -> FileService
                .getInstance()
                .readDataFromStream(inputStream,
                        dataWithVersion -> EventBusUtils.STATISTICS_LOADED_EVENT.trigger(new StatisticsWithIdsAndPath(path, (List<StatisticsWithId>) dataWithVersion.getObject()))
                )).thenRun(() -> Platform.runLater(() -> EventBusUtils.LOG_LABEL_EVENT.trigger("Statistics loaded from file")))
                .exceptionally(throwable -> {
                    log.error("Loading statistics data failed", throwable);
                    Platform.runLater(() -> EventBusUtils.LOG_LABEL_EVENT.trigger(String.format("Loading failed: %s", throwable.getMessage())));
                    return null;
                });
    }

    public InputStream getHuntFileWithIdAsStream(int id, Path path) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(path));
        String fileName = id + HUNT_FILE_EXTENSION;
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.getName().equals(fileName)) {
                return zipInputStream;
            }
        }
        throw new FileNotFoundException(String.format("No %s file found in %s", fileName, path.toString()));
    }
}
