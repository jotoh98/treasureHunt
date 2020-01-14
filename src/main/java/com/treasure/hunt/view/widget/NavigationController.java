package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.Getter;

public class NavigationController {
    @Getter
    private ObjectProperty<GameManager> gameManager = new SimpleObjectProperty<>();

    @FXML
    private Button previousButton;

    @FXML
    private TextField stepCountField;

    @FXML
    private Label stepCountLabel;

    @FXML
    private Button nextButton;

    public void initialize() {
        gameManager.addListener(c -> {
            if (gameManager.isNull().get()) {
                return;
            }

            GameManager managerInstance = gameManager.get();

            nextButton.disableProperty().bind(managerInstance.getStepForwardImpossibleBinding());
            previousButton.disableProperty().bind(managerInstance.getStepBackwardImpossibleBinding());
            stepCountField.disableProperty().bind(gameManager.isNull());
            stepCountLabel.setText("of " + (managerInstance.getMoveSizeBinding().get() - 1));
            stepCountField.setText(Integer.toString(managerInstance.getViewIndex().get()));
            managerInstance.getMoveSizeBinding().addListener(
                    (obs, oldValue, newValue) -> stepCountLabel.setText("of " + (((int) newValue) - 1))
            );

            managerInstance.getViewIndex().addListener(
                    invalidation -> stepCountField.setText(Integer.toString(managerInstance.getViewIndex().get()))
            );

        });
    }

    @FXML
    void nextButtonClicked(ActionEvent event) {
        gameManager.get().next();
    }

    @FXML
    void previousButtonClicked(ActionEvent event) {
        gameManager.get().previous();
    }

    public void onEnter(ActionEvent actionEvent) {
        int input;
        final int lastIndex = gameManager.get().getMoveSizeBinding().get() - 1;

        try {
            input = Integer.parseInt(stepCountField.getText());
        } catch (NumberFormatException e) {
            input = lastIndex;
        }

        final int newIndex = Math.max(0, Math.min(lastIndex, input));

        gameManager.get().getViewIndex().set(newIndex);
        stepCountField.setText(String.valueOf(newIndex));
        stepCountLabel.requestFocus();
    }
}
