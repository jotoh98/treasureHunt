package com.treasure.hunt.service.io;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.treasure.hunt.analysis.StatisticsWithId;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.utils.EventBusUtils;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author axel1200
 */
@Slf4j
public class FileService {
    private static FileService instance;
    private final FileChooser fileChooser;

    private FileService() {
        fileChooser = new FileChooser();
        fileChooser.setInitialFileName("saved.hunt");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("hunt instance files (*.hunt)", "*.hunt");
        fileChooser.getExtensionFilters().add(extFilter);
    }

    private Kryo newKryo() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        return kryo;
    }

    public synchronized static FileService getInstance() {
        if (instance == null) {
            instance = new FileService();
        }
        return instance;
    }

    public void writeGameDataToFile(GameManager gameManager, Path filePath) throws IOException {
        Output output = new Output(new FileOutputStream(filePath.toFile()));
        newKryo().writeObject(output, new DataWithVersion(GameManager.class.getPackage().getImplementationVersion(), gameManager));
        output.close();
    }

    public void writeGameDataToOutputStream(GameManager gameManager, OutputStream outputStream) throws IOException {
        Output output = new Output(outputStream);
        newKryo().writeObject(output, new DataWithVersion(GameManager.class.getPackage().getImplementationVersion(), gameManager));
        output.flush();
    }

    public void writeStatisticsWithId(List<StatisticsWithId> statisticsWithIds, OutputStream outputStream) throws IOException {
        Output output = new Output(outputStream);
        newKryo().writeObject(output, new DataWithVersion(StatisticsWithId.class.getPackage().getImplementationVersion(), statisticsWithIds));
        output.flush();
    }

    public void readGameManagerFromPathAndLoad(Path filePath) throws IOException {
        readDataFromFile(filePath
                , dataWithVersion -> EventBusUtils.GAME_MANAGER_LOADED_EVENT.trigger((GameManager) dataWithVersion.getObject()));
    }

    public void readGameManagerFromStreamAndLoad(InputStream inputStream) throws IOException {
        readDataFromStream(inputStream
                , dataWithVersion -> EventBusUtils.GAME_MANAGER_LOADED_EVENT.trigger((GameManager) dataWithVersion.getObject()));
    }

    public void readDataFromFile(Path filePath, Consumer<DataWithVersion> finishedCallBack) throws IOException {
        readDataFromStream(new FileInputStream(filePath.toFile()), finishedCallBack);
    }

    @SneakyThrows
    public void readDataFromStream(InputStream inputStream, Consumer<DataWithVersion> finishedCallBack) {
        Input input = new Input(inputStream);
        DataWithVersion dataWithVersion = newKryo().readObject(input, DataWithVersion.class);
        input.close();
        if (correctVersion(dataWithVersion)) {
            Platform.runLater(() -> {
                if (askUserWhetherToLoadWrongVersion(dataWithVersion.getVersion())) {
                    finishedCallBack.accept(dataWithVersion);
                }
            });
            return;
        }
        finishedCallBack.accept(dataWithVersion);
    }

    public void loadGameManager() {
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile == null) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                readGameManagerFromPathAndLoad(selectedFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).thenRun(() -> Platform.runLater(() -> EventBusUtils.LOG_LABEL_EVENT.trigger("Game state loaded from file")))
                .exceptionally(throwable -> {
                    log.error("Loading game data failed", throwable);
                    Platform.runLater(() -> EventBusUtils.LOG_LABEL_EVENT.trigger(String.format("Loading failed: %s", throwable.getMessage())));
                    return null;
                });
    }

    private boolean correctVersion(DataWithVersion dataWithVersion) {
        boolean divergentVersion = dataWithVersion.getVersion() != null && !dataWithVersion.getVersion().equals(GameManager.class.getPackage().getImplementationVersion());
        boolean loadingDevelopmentVersion = dataWithVersion.getVersion() == null && GameManager.class.getPackage().getImplementationVersion() != null;

        return divergentVersion || loadingDevelopmentVersion;
    }

    private boolean askUserWhetherToLoadWrongVersion(String oldVersion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Proceed loading wrong version");
        alert.setHeaderText("The file was record with a different version than your program is running. Loading it might cause unexpected behaviour.");
        alert.setContentText("Version of file was " + (oldVersion == null ? "development" : oldVersion));

        Optional<ButtonType> option = alert.showAndWait();

        ButtonType buttonType = option.get();
        if (buttonType == ButtonType.OK) {
            return true;
        } else if (buttonType == ButtonType.CANCEL) {
            return false;
        } else {
            return false;
        }
    }

    public void saveGameManager(GameManager gameManager) {
        if (gameManager == null) {
            EventBusUtils.LOG_LABEL_EVENT.trigger("No game manager to save");
            return;
        }
        File selectedFile = fileChooser.showSaveDialog(new Stage());
        if (selectedFile == null) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                writeGameDataToFile(gameManager, selectedFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).thenRun(() -> Platform.runLater(() -> EventBusUtils.LOG_LABEL_EVENT.trigger("Game state written to file")))
                .exceptionally(throwable -> {
                    log.error("Saving game data failed", throwable);
                    Platform.runLater(() -> EventBusUtils.LOG_LABEL_EVENT.trigger(String.format("Saving failed: %s", throwable.getMessage())));
                    return null;
                });
    }

    @Value
    public static class DataWithVersion<E> {
        String version;
        E object;
    }


}
