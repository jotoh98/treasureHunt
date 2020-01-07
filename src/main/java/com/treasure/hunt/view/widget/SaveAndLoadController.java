package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.service.FileService;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import lombok.extern.slf4j.Slf4j;

/**
 * @author axel1200
 */
@Slf4j
public class SaveAndLoadController {

    public Spinner roundSpinner;
    public ProgressIndicator progressIndicator;
    private ObjectProperty<GameManager> gameManager;
    private Label logLabel;

    public void initialize() {

    }

    public void init(ObjectProperty<GameManager> gameManager, Label logLabel) {
        this.gameManager = gameManager;
        this.logLabel = logLabel;
    }

    public void save(ActionEvent actionEvent) {
        FileService.getInstance().save(logLabel, gameManager.get());
    }

    public void load(ActionEvent actionEvent) {
        FileService.getInstance().load(logLabel);
    }

    public void onSeriesRun(ActionEvent actionEvent) {

    }
}

