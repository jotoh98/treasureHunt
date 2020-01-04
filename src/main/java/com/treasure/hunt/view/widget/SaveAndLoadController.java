package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.utils.JsonFileUtils;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author axel1200
 */
@Slf4j
public class SaveAndLoadController {

    private static final FileChooser fileChooser;

    static {
        fileChooser = new FileChooser();
        fileChooser.setInitialFileName(String.format("saved_%s.hunt", OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE)));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("hunt instance files (*.hunt)", "*.hunt");
        fileChooser.getExtensionFilters().add(extFilter);
    }

    private ObjectProperty<GameManager> gameManager;
    private Label logLabel;

    public void init(ObjectProperty<GameManager> gameManager, Label logLabel) {
        this.gameManager = gameManager;
        this.logLabel = logLabel;
    }

    public void save(ActionEvent actionEvent) {
        if (gameManager.get() == null) {
            logLabel.setText("No game manager to save");
            return;
        }
        File selectedFile = fileChooser.showSaveDialog(new Stage());
        try {
            JsonFileUtils.writeGameDataToFile(gameManager.get(), selectedFile.toPath());
        } catch (Exception e) {
            log.error("Saving game data failed", e);
            logLabel.setText(String.format("Saving failed: %s", e.getMessage()));
        }
    }

    public void load(ActionEvent actionEvent) {

    }
}

