package com.treasure.hunt.service.select;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.awt.PointTransformation;
import com.treasure.hunt.jts.geom.Circle;
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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SelectionService {
    private static final SelectionService instance = new SelectionService();
    private static final double MOUSE_RECOGNIZE_DISTANCE = 4;

    private GameManager currentGameManager;
    private GeometryItem<?> geometryItemSelected;
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
                currentGameManager.removeAdditional("Highlighter");
            }
            EventBusUtils.GEOMETRY_ITEM_SELECTED.trigger(null);

            geometryItemSelected = null;
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
                .filter(geometryItem -> geometryItem.getGeometryType().isSelectable())
                .filter(geometryItem -> {
                    final Object object = geometryItem.getObject();
                    double foundDistance = Double.POSITIVE_INFINITY;
                    if (object instanceof Geometry) {
                        foundDistance = ((Geometry) object).distance(JTSUtils.createPoint(coordinate.getX(), coordinate.getY()));
                    } else if (object instanceof Circle) {
                        return ((Circle) object).inside(coordinate);
                    }
                    return foundDistance <= distance;
                })
                .collect(Collectors.toList());

        if (mouseEvent.isShiftDown()) {
            if (foundItems.contains(geometryItemSelected)) {
                if (geometryItemSelected != null) {
                    gameManager.removeAdditional("Highlighter");
                }
                EventBusUtils.GEOMETRY_ITEM_SELECTED.trigger(null);
                geometryItemSelected = null;
            }
            return;
        }

        if (foundItems.isEmpty()) {
            if (geometryItemSelected != null) {
                gameManager.removeAdditional("Highlighter");
            }
            EventBusUtils.GEOMETRY_ITEM_SELECTED.trigger(null);

            geometryItemSelected = null;
            return;
        }

        foundItems.remove(geometryItemSelected);

        if (foundItems.size() == 1) {
            GeometryItem<?> geometryItem = foundItems.get(0);
            selectItem(gameManager, geometryItem);
            return;
        }

        sortCirclesByRadius(foundItems);

        SelectClickedPopUp selectClickedPopUp = new SelectClickedPopUp();
        EventBusUtils.INNER_POP_UP_EVENT.trigger(new Pair<>(selectClickedPopUp.getPopUp(), new Pair<>(mouseEvent.getScreenX(), mouseEvent.getScreenY())));

        selectClickedPopUp.getCorrectItem(foundItems).thenAccept(geometryItem -> selectItem(gameManager, geometryItem));
    }

    private void sortCirclesByRadius(final List<GeometryItem<?>> foundItems) {
        foundItems.sort(Comparator.comparing(geometryItem -> {
            if (!(geometryItem.getObject() instanceof Circle)) {
                return Integer.MAX_VALUE;
            }
            final Circle circle = (Circle) geometryItem.getObject();
            return (int) circle.getRadius();
        }));
    }

    private void selectItem(GameManager gameManager, GeometryItem<?> geometryItem) {
        geometryItemSelected = geometryItem;
        EventBusUtils.GEOMETRY_ITEM_SELECTED.trigger(geometryItemSelected);
        gameManager.addAdditional("Highlighter", createEnvelope(geometryItemSelected.getObject()));
    }

    private GeometryItem<?> createEnvelope(Object item) {
        if (item instanceof Point) {
            return new GeometryItem<>(
                    JTSUtils.createPoint(((Point) item).getX(), ((Point) item).getY()),
                    GeometryType.HIGHLIGHTER);
        }
        if (item instanceof Circle) {
            return new GeometryItem<>(
                    JTSUtils.toPolygon(((Circle) item).getEnvelope()),
                    GeometryType.HIGHLIGHTER);
        }
        if (item instanceof Geometry) {
            return new GeometryItem<>(
                    JTSUtils.toPolygon(((Geometry) item).getEnvelopeInternal()),
                    GeometryType.HIGHLIGHTER);
        }

        return null;

    }
}
