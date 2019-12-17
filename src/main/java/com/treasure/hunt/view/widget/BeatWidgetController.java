package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeatWidgetController {

    final int initialValue = 3;
    public Spinner<Integer> timeBetweenMovesSpinner;
    private ObjectProperty<GameManager> gameManager;
    private Label logLabel;

    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, initialValue);
        timeBetweenMovesSpinner.setValueFactory(valueFactory);
    }

    public void init(ObjectProperty<GameManager> gameManager, Label logLabel) {
        this.gameManager = gameManager;
        this.logLabel = logLabel;
    }

    public void play(ActionEvent actionEvent) {
        gameManager.get().beat(timeBetweenMovesSpinner.getValue());
        logLabel.setText("Game running.");
    }

    public void stop(ActionEvent actionEvent) {
        gameManager.get().stopBeat();
        logLabel.setText("Game stopped.");
    }
}

