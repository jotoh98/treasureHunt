package com.treasure.hunt.service.select;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.awt.PointTransformation;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.utils.EventBusUtils;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.view.SelectClickedPopUp;
import javafx.beans.InvalidationListener;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import lombok.SneakyThrows;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.stream.Collectors;

public class SelectionService {
    private static final SelectionService instance = new SelectionService();
    private static final double MOUSE_RECOGNIZE_DISTANCE = 4;

    private GameManager currentGameManager;
    private Geometry geometrySelected;
    private GeometryItem<? extends Geometry> geometryItemSelected;
    private InvalidationListener stepChangeListener = observable -> stepChanged();

    private SelectionService() {

    }

    public static SelectionService getInstance() {
        return instance;
    }

    private void stepChanged() {
        if (currentGameManager.getVisibleGeometries()
                .noneMatch(geometryItem -> geometryItem.equals(geometryItemSelected))) {
            if (geometryItemSelected != null) {
                currentGameManager.getAdditional()
                        .remove("Highlighter");
            }
            EventBusUtils.GEOMETRY_ITEM_SELECTED.trigger(null);

            geometryItemSelected = null;
            geometrySelected = null;
        }
    }

    @SneakyThrows
    public void handleClickEvent(MouseEvent mouseEvent, PointTransformation transformation, GameManager gameManager) {
        if (gameManager == null) {
            return;
        }

        Coordinate coordinate = transformation.revert(mouseEvent.getX(), mouseEvent.getY());

        if (currentGameManager != gameManager) {
            gameManager.getViewIndex().addListener(stepChangeListener);
            if (currentGameManager != null) {
                currentGameManager.getViewIndex().removeListener(stepChangeListener);
            }
            currentGameManager = gameManager;
        }

        double distance = MOUSE_RECOGNIZE_DISTANCE / transformation.getScale();

        List<GeometryItem<?>> foundItems = gameManager
                .getVisibleGeometries()
                .filter(geometryItem -> geometryItem.getObject() instanceof Geometry && geometryItem.getGeometryType() != GeometryType.HIGHLIGHTER)
                .filter(geometryItem -> ((Geometry) geometryItem.getObject()).distance(JTSUtils.createPoint(coordinate.getX(), coordinate.getY())) <= distance)
                .collect(Collectors.toList());

        if (mouseEvent.isShiftDown()) {
            if (foundItems.contains(geometryItemSelected)) {
                if (geometryItemSelected != null) {
                    gameManager.getAdditional()
                            .remove("Highlighter");
                }
                EventBusUtils.GEOMETRY_ITEM_SELECTED.trigger(null);
                geometryItemSelected = null;
                geometrySelected = null;
            }
            return;
        }

        if (foundItems.isEmpty()) {
            if (geometryItemSelected != null) {
                gameManager.getAdditional()
                        .remove("Highlighter");
            }
            EventBusUtils.GEOMETRY_ITEM_SELECTED.trigger(null);

            geometryItemSelected = null;
            geometrySelected = null;
            return;
        }

        if (foundItems.size() == 1) {
            GeometryItem<? extends Geometry> geometryItem = (GeometryItem<? extends Geometry>) foundItems.get(0);
            selectItem(gameManager, geometryItem);
            return;
        }

        foundItems.remove(geometryItemSelected);

        SelectClickedPopUp selectClickedPopUp = new SelectClickedPopUp();
        EventBusUtils.INNER_POP_UP_EVENT.trigger(new Pair<>(selectClickedPopUp.getPopUp(), new Pair<>(mouseEvent.getScreenX(), mouseEvent.getScreenY())));

        selectClickedPopUp.getCorrectItem(foundItems).thenAccept(geometryItem -> selectItem(gameManager, (GeometryItem<? extends Geometry>) geometryItem));
    }

    private void selectItem(GameManager gameManager, GeometryItem<? extends Geometry> geometryItem) {
        geometryItemSelected = geometryItem;
        geometrySelected = geometryItemSelected.getObject();
        EventBusUtils.GEOMETRY_ITEM_SELECTED.trigger(geometryItemSelected);
        gameManager.getAdditional()
                .put("Highlighter", createEnvelope(geometrySelected));
    }

    private GeometryItem<?> createEnvelope(Geometry item) {
        if (item instanceof Point) {
            return new GeometryItem<>(
                    JTSUtils.createPoint(((Point) item).getX(), ((Point) item).getY()),
                    GeometryType.HIGHLIGHTER);
        }
        return new GeometryItem<>(
                JTSUtils.toPolygon(item.getEnvelopeInternal()),
                GeometryType.HIGHLIGHTER);
    }
}
