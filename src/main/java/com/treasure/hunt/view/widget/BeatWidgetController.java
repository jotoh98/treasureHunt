package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dorianreineccius
 */
@Slf4j
public class BeatWidgetController {

    final int initialValue = 1;
    public Spinner<Double> timeBetweenMovesSpinner;
    public Button playToggle;
    private ObjectProperty<GameManager> gameManager;
    private Label logLabel;

    public void initialize() {
        SpinnerValueFactory<Double> valueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10, initialValue, 0.25);
        timeBetweenMovesSpinner.setValueFactory(valueFactory);
    }

    private void bindPlayToggleButton() {
        InvalidationListener listener = c -> {
            if (gameManager.isNull().get()) {
                return;
            }

            playToggle.textProperty().bind(Bindings.createStringBinding(
                    () -> {
                        boolean impossible = gameManager.get().getStepForwardImpossibleBinding().get();
                        if (impossible) {
                            return "Game finished";
                        }
                        boolean running = gameManager.get().getBeatThreadRunning().get();

                        return running ? "Stop" : "Start";
                    },
                    gameManager.get().getBeatThreadRunning(),
                    gameManager.get().getStepForwardImpossibleBinding()
            ));

            playToggle.disableProperty().bind(gameManager.get().getStepForwardImpossibleBinding());
        };
        gameManager.addListener(listener);
        listener.invalidated(gameManager);
    }

    public void init(ObjectProperty<GameManager> gameManager, Label logLabel) {
        this.gameManager = gameManager;
        this.logLabel = logLabel;
        bindPlayToggleButton();
    }

    public void playToggle(ActionEvent actionEvent) {
        GameManager gameManagerState = this.gameManager.get();

        if (gameManagerState.getBeatThreadRunning().get()) {
            gameManagerState.stopBeat();
        } else {
            gameManagerState.beat(timeBetweenMovesSpinner.valueProperty());
        }
    }
}

