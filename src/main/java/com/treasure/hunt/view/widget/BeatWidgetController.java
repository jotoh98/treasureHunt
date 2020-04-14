package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

            gameManager.get().getBeatThreadRunning().addListener((observable, oldValue, newValue) -> {
                ImageView imageView = (ImageView) playToggle.getGraphic();
                imageView.setImage(new Image("images/icon/" + (newValue ? "pause" : "play") + ".png"));
            });

            playToggle.disableProperty().bind(gameManager.get().getStepForwardImpossibleBinding());
        };
        gameManager.addListener(listener);
        listener.invalidated(gameManager);
    }

    public void init(ObjectProperty<GameManager> gameManager) {
        this.gameManager = gameManager;
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

