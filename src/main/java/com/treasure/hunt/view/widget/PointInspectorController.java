package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.service.select.SelectionService;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.utils.EventBusUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.util.Arrays;
import java.util.stream.Collectors;

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
    public Label selectedCoordinates;
    public Label selectedType;
    public Button selectButton;
    public Label noSelectedLabel;
    public GridPane selectedPane;
    private ObjectProperty<GameManager> gameManager;

    public void init(ObjectProperty<GameManager> gameManager) {
        this.gameManager = gameManager;
        treasurePointBinding();

        setSelectedItemAvailable(false);
        gameManager.addListener(observable -> {
            renewBinding();
            setSelectedItemAvailable(false);
        });
        renewBinding();
        modeSelection.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> renewBinding());
        modeSelection.getItems().setAll(PointType.TREASURE, PointType.WAY_POINT);
        modeSelection.getSelectionModel().select(0);

        selectButton.disableProperty().bind(SelectionService.getInstance().getSelectionInProgress());
        selectButton.textProperty()
                .bind(Bindings.createStringBinding(() ->
                                SelectionService.getInstance().getSelectionInProgress().get() ? "Click on an item to select" : "Click here to start selecting",
                        SelectionService.getInstance().getSelectionInProgress()));
        selectedPane.managedProperty().bind(selectedPane.visibleProperty());
        noSelectedLabel.managedProperty().bind(noSelectedLabel.visibleProperty());
        EventBusUtils.GEOMETRY_ITEM_SELECTED.addListener(this::itemSelected);
    }

    private void setSelectedItemAvailable(boolean available) {
        noSelectedLabel.setVisible(!available);
        selectedPane.setVisible(available);
    }

    private void itemSelected(GeometryItem<? extends Geometry> geometrySelected) {
        if (geometrySelected == null) {
            setSelectedItemAvailable(false);
            return;
        }
        setSelectedItemAvailable(true);
        String coordinateString = Arrays.stream(geometrySelected.getObject().getCoordinates()).map(Coordinate::toString).collect(Collectors.joining(","));
        selectedCoordinates.setText(coordinateString);
        selectedType.setText(geometrySelected.getGeometryType().getDisplayName());
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

    public void selectNow() {
        SelectionService.getInstance().getSelectionInProgress().setValue(true);
    }

    private enum PointType {
        TREASURE,
        WAY_POINT
    }
}

