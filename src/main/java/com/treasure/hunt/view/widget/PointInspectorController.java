package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;

/**
 * @author axel1200
 */
@Slf4j
public class PointInspectorController {

    @FXML
    public TextField xCoordinateLabel;
    @FXML
    public TextField yCoordinateLabel;
    public ComboBox<PointType> modeSelection;
    private ObjectProperty<GameManager> gameManager;

    public void init(ObjectProperty<GameManager> gameManager) {
        this.gameManager = gameManager;
        treasurePointBinding();
        gameManager.addListener(observable -> {
            renewBinding();
        });
        modeSelection.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> renewBinding());
        modeSelection.getItems().setAll(PointType.TREASURE, PointType.WAY_POINT);
    }

    private void renewBinding() {
        PointType selectedItem = modeSelection.getSelectionModel().getSelectedItem();
        if (selectedItem == PointType.WAY_POINT) {
            wayPointPointBinding();
        } else if (selectedItem == PointType.TREASURE) {
            treasurePointBinding();
        }
    }

    private void wayPointPointBinding() {
        if (gameManager.get() == null) {
            return;
        }

        ObjectBinding<Point> lastTreasureBinding = gameManager.get().getLastPointBinding();

        bindPoint(lastTreasureBinding);
    }

    private void treasurePointBinding() {
        if (gameManager.get() == null) {
            return;
        }

        ObjectBinding<Point> lastTreasureBinding = gameManager.get().getLastTreasureBindings();

        bindPoint(lastTreasureBinding);
    }

    public void bindPoint(ObjectBinding<Point> point) {
        StringBinding xStringBinding = Bindings.createStringBinding(
                () -> String.valueOf(point.get().getX()),
                point
        );

        StringBinding yStringBinding = Bindings.createStringBinding(
                () -> String.valueOf(point.get().getY()),
                point
        );
        xCoordinateLabel.textProperty().bind(xStringBinding);
        yCoordinateLabel.textProperty().bind(yStringBinding);
    }

    private enum PointType {
        TREASURE,
        WAY_POINT
    }
}

