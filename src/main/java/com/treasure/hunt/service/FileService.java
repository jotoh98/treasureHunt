package com.treasure.hunt.service;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.utils.EventBusUtils;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author axel1200
 */
@Slf4j
public class FileService {

    private static FileService instance;
    private final Kryo kryo;
    private final FileChooser fileChooser;

    private FileService() {
        fileChooser = new FileChooser();
        fileChooser.setInitialFileName("saved.hunt");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("hunt instance files (*.hunt)", "*.hunt");
        fileChooser.getExtensionFilters().add(extFilter);

        kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
    }

    public static FileService getInstance() {
        if (FileService.instance == null) {
            FileService.instance = new FileService();
        }
        return FileService.instance;
    }

    public void writeGameDataToFile(GameManager gameManager, Path filePath) throws IOException {
        Output output = new Output(new FileOutputStream(filePath.toFile()));
        kryo.writeObject(output, new GameManagerWithVersion(GameManager.class.getPackage().getImplementationVersion(), gameManager));
        output.close();
    }

    public GameManagerWithVersion readGameDataFromFile(Path filePath) throws IOException {
        Input input = new Input(new FileInputStream(filePath.toFile()));
        GameManagerWithVersion gameManagerWithVersion = kryo.readObject(input, GameManagerWithVersion.class);
        input.close();
        return gameManagerWithVersion;
    }

    public void load(Label logLabel) {
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile == null) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                GameManagerWithVersion gameManagerWithVersion = readGameDataFromFile(selectedFile.toPath());
                boolean divergentVersion = gameManagerWithVersion.getVersion() != null && !gameManagerWithVersion.getVersion().equals(GameManager.class.getPackage().getImplementationVersion());
                boolean loadingDevelopmentVersion = gameManagerWithVersion.getVersion() == null && GameManager.class.getPackage().getImplementationVersion() != null;
                if (divergentVersion || loadingDevelopmentVersion) {
                    Platform.runLater(() -> {
                        if (askUserWhetherToLoadWrongVersion(gameManagerWithVersion.getVersion())) {
                            EventBusUtils.GAME_MANAGER_LOADED_EVENT.trigger(gameManagerWithVersion.getGameManager());
                        }
                    });
                    return;
                }
                EventBusUtils.GAME_MANAGER_LOADED_EVENT.trigger(gameManagerWithVersion.getGameManager());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).thenRun(() -> Platform.runLater(() -> logLabel.setText("Game state loaded from file")))
                .exceptionally(throwable -> {
                    log.error("Loading game data failed", throwable);
                    Platform.runLater(() -> logLabel.setText(String.format("Loading failed: %s", throwable.getMessage())));
                    return null;
                });
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

    public void save(Label logLabel, GameManager gameManager) {
        if (gameManager == null) {
            logLabel.setText("No game manager to save");
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
        }).thenRun(() -> Platform.runLater(() -> logLabel.setText("Game state written to file")))
                .exceptionally(throwable -> {
                    log.error("Saving game data failed", throwable);
                    Platform.runLater(() -> logLabel.setText(String.format("Saving failed: %s", throwable.getMessage())));
                    return null;
                });
    }

    @Value
    public static class GameManagerWithVersion {
        String version;
        GameManager gameManager;
    }


}
