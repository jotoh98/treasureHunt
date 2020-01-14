package com.treasure.hunt.service;

import com.esotericsoftware.kryo.Kryo;
import com.treasure.hunt.analysis.StatisticsWithId;
import com.treasure.hunt.analysis.StatisticsWithIdsAndPath;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.io.FileService;
import com.treasure.hunt.utils.AsyncUtils;
import com.treasure.hunt.utils.EventBusUtils;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public class SeriesService {
    public static final String STATS_FILE_NAME = "stats.huntstats";
    public static final String HUNT_FILE_EXTENSION = ".hunt";
    private static SeriesService instance;
    private static final int SMALL_ROUND_SIZE = 10000;

    private final FileChooser fileChooser;

    private SeriesService() {
        fileChooser = new FileChooser();
        fileChooser.setInitialFileName("saved.hunts");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("hunt series files (*.hunts)", "*.hunts");
        fileChooser.getExtensionFilters().add(extFilter);
    }

    private Kryo newKryo() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        return kryo;
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
        runSeriesAndSaveToFile(rounds, gameManager, progressConsumer, selectedFile.get(), false, true);
    }

    /**
     * @param rounds           amount of runs
     * @param gameManager      gameManager to be copied (preserves state for multiple starts with same states)
     * @param progressConsumer consumer for working progress, We have 4 workload points per run 1 for copying GameManager, 6 for the actual run and 2 for writing the file
     * @param selectedFile     the file the runs are written to
     */
    @SneakyThrows
    public void runSeriesAndSaveToFile(Integer rounds, GameManager gameManager, Consumer<Double> progressConsumer, File selectedFile, boolean alreadyInitialed, boolean writeGameManger) {
        int totalWorkLoad = rounds * (writeGameManger ? 9 : 6);

        AtomicInteger workLoadDone = new AtomicInteger();
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(selectedFile));
        ExecutorService executorService = AsyncUtils.newExhaustingThreadPoolExecutor();
        List<CompletableFuture<Void>> runFutures = new ArrayList<>(SMALL_ROUND_SIZE);
        List<StatisticsWithId> statisticsWithIds = new ArrayList<>(rounds);
        for (int i = 0; i < rounds; i++) {
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(duplicateGameManager(gameManager, progressConsumer, alreadyInitialed, totalWorkLoad, workLoadDone), executorService)
                    .thenApplyAsync(runGame(progressConsumer, totalWorkLoad, workLoadDone), executorService)
                    .thenAcceptAsync(writeGameManagerAndSaveStats(writeGameManger, progressConsumer, totalWorkLoad, workLoadDone, zipOutputStream, statisticsWithIds), executorService);
            runFutures.add(future);
            if (i % SMALL_ROUND_SIZE == 0 && i != 0) {
                CompletableFuture<Void> allRunsFinished = CompletableFuture.allOf(runFutures.toArray(new CompletableFuture[runFutures.size()]));
                allRunsFinished.join();
                runFutures.clear();
            }
        }

        CompletableFuture<Void> allRunsFinished = CompletableFuture.allOf(runFutures.toArray(new CompletableFuture[runFutures.size()]));
        allRunsFinished.join();
        runFutures.clear();
        writeStatisticsFile(statisticsWithIds, zipOutputStream);

        zipOutputStream.close();
    }

    @NotNull
    private Consumer<GameManager> writeGameManagerAndSaveStats(boolean writeGameManger, Consumer<Double> progressConsumer, int totalWorkLoad, AtomicInteger workLoadDone, ZipOutputStream zipOutputStream, List<StatisticsWithId> statisticsWithIds) {
        return gameManagerCopy -> {
            synchronized (zipOutputStream) {
                int id = statisticsWithIds.size() - 1;
                statisticsWithIds.add(new StatisticsWithId(id, gameManagerCopy.getStatistics().get()));
                try {
                    if (writeGameManger) {
                        writeGameManager(id, progressConsumer, workLoadDone, totalWorkLoad, zipOutputStream, gameManagerCopy);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @NotNull
    private Function<GameManager, GameManager> runGame(Consumer<Double> progressConsumer, double totalWorkLoad, AtomicInteger workLoadDone) {
        return gameManagerCopy -> {
            CompletableFuture<Void> beat = gameManagerCopy.beat().thenRun(() -> {
                synchronized (workLoadDone) {
                    workLoadDone.addAndGet(5);
                    progressConsumer.accept(workLoadDone.get() / totalWorkLoad);
                }
            });
            beat.join();
            return gameManagerCopy;
        };
    }

    @NotNull
    private Supplier<GameManager> duplicateGameManager(GameManager gameManager, Consumer<Double> progressConsumer, boolean alreadyInitialed, double totalWorkLoad, AtomicInteger workLoadDone) {
        return () -> {
            GameManager gameMangerCopy;
            gameMangerCopy = newKryo().copy(gameManager);

            if (!alreadyInitialed) {
                gameMangerCopy.init();
            }
            synchronized (workLoadDone) {
                progressConsumer.accept(workLoadDone.incrementAndGet() / totalWorkLoad);
            }
            return gameMangerCopy;
        };
    }


    private void writeStatisticsFile(List<StatisticsWithId> statisticsWithIds, ZipOutputStream zipOutputStream) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(STATS_FILE_NAME));
        FileService.getInstance()
                .writeStatisticsWithId(new ArrayList<>(statisticsWithIds), zipOutputStream);
    }


    private void writeGameManager(int id, Consumer<Double> progressConsumer, AtomicInteger workLoadDone, double totalWorkLoad, ZipOutputStream zipOutputStream, GameManager gameManager) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(id + HUNT_FILE_EXTENSION));
        FileService.getInstance().writeGameDataToOutputStream(gameManager, zipOutputStream);
        synchronized (workLoadDone) {
            workLoadDone.addAndGet(3);
            progressConsumer.accept(workLoadDone.get() / totalWorkLoad);
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
