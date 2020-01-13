package com.treasure.hunt.view;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.PointTransformation;
import com.treasure.hunt.jts.geom.CircleHighlighter;
import com.treasure.hunt.jts.geom.RectangleVariableHighlighter;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.utils.EventBusUtils;
import com.treasure.hunt.utils.JTSUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jfree.fx.FXGraphics2D;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author axel12, dorianreineccius
 */
@Slf4j
public class CanvasController {
    /**
     * The maximum distance on canvas between the mouse and a {@link GeometryItem},
     * in which the mouse can select a {@link GeometryItem} on click.
     */
    public static final double MOUSE_RECOGNIZE_DISTANCE = 5;
    private Coordinate lastMouseClick;
    private GeometryItem highlighter;
    private List<GeometryItem> geometryItemsList = new ArrayList<>();
    private int geometryItemsListIndex = 0;
    /**
     * Determines, whether the mouse was dragged.
     */
    private boolean dragged = false;

    @Getter
    public Canvas canvas;
    public Pane canvasPane;
    private ObjectProperty<GameManager> gameManager;

    @Getter
    private PointTransformation transformation = new PointTransformation();
    private AdvancedShapeWriter shapeWriter = new AdvancedShapeWriter(transformation);

    private FXGraphics2D graphics2D;

    private Vector2D dragStart = new Vector2D();
    private Vector2D offsetBackup = new Vector2D();

    public void initialize() {
        makeCanvasResizable();
        graphics2D = new FXGraphics2D(canvas.getGraphicsContext2D());

        transformation.getScaleProperty().addListener(invalidation -> drawShapes());

        transformation.getOffsetProperty().addListener(invalidation -> drawShapes());

        subscribeToGeometryItem();
    }

    /**
     * Subscribes to {@link EventBusUtils#GAME_MANAGER_LOADED_EVENT}
     * in order to handle its {@link GeometryItem}s.
     */
    public void subscribeToGeometryItem() {
        EventBusUtils.SELECTED_GEOMETRY_ITEMS_EVENT.addListener(geometryItem -> {
            Platform.runLater(() -> {
                log.trace("received: " + geometryItem);
                Geometry geometry = (Geometry) geometryItem.getObject();
                if (geometry instanceof Point) {
                    this.highlighter = new GeometryItem(new CircleHighlighter(geometry.getCoordinate(),
                            CanvasController.MOUSE_RECOGNIZE_DISTANCE, 64, JTSUtils.GEOMETRY_FACTORY), GeometryType.HIGHLIGHTER);
                } else if (geometryItem.getObject() instanceof LineString) {
                    double minX = geometry.getCoordinates()[0].x;
                    double maxX = minX;
                    double minY = geometry.getCoordinates()[0].y;
                    double maxY = minY;
                    for (Coordinate coordinate : geometry.getCoordinates()) {
                        minX = Math.min(minX, coordinate.x);
                        maxX = Math.max(maxX, coordinate.x);
                        minY = Math.min(minY, coordinate.x);
                        maxY = Math.max(maxY, coordinate.x);
                    }
                    this.highlighter = new GeometryItem(new RectangleVariableHighlighter(
                            new Coordinate(minX, maxY), maxX - minX, maxY - minY, JTSUtils.GEOMETRY_FACTORY), GeometryType.HIGHLIGHTER);
                }
                drawShapes();
            });
        });
    }

    public void makeCanvasResizable() {
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> {
            transformation.updateCanvasWidth((double) newValue);
            drawShapes();
        });

        canvas.heightProperty().addListener((observable, oldValue, newValue) -> {
            transformation.updateCanvasHeight((double) newValue);
            drawShapes();
        });

        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvas.widthProperty().bind(canvasPane.widthProperty());
    }

    /**
     * This method clears and draws all current {@link GeometryItem} given by the {@link GameManager} on the canvas.
     */
    void drawShapes() {
        Platform.runLater(() -> {
            if (gameManager == null) {
                return;
            }
            if (gameManager.isNotNull().get()) {
                deleteShapes();
                gameManager.get().getGeometryItems(true).forEach(geometryItem ->
                        geometryItem.draw(graphics2D, shapeWriter)
                );
                if (this.highlighter != null) {
                    this.highlighter.draw(graphics2D, shapeWriter);
                }
            }
        });
    }

    /**
     * This clears the {@link GeometryItem}'s from the canvas.
     */
    private void deleteShapes() {
        if (gameManager == null) {
            return;
        }
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Executed when the mouse was pressed and released.
     * TODO To much game logic in here!!11 maybe wrap into GameManager
     * Executing this will select a {@link GeometryItem} nearest to the mouse position,
     * with a maximum distance of {@link CanvasController#MOUSE_RECOGNIZE_DISTANCE}.
     *
     * @param mouseEvent corresponding {@link MouseEvent}
     */
    public void onCanvasClicked(MouseEvent mouseEvent) {
        if (gameManager == null) {
            return;
        }
        offsetBackup = transformation.getOffsetProperty().get();

        if (dragged) {
            dragged = false;
            return;
        }
        dragged = false;

        Vector2D mousePositionInGameContext = dragStart.subtract(offsetBackup);
        mousePositionInGameContext = mousePositionInGameContext.divide(transformation.getScaleProperty().get());

        if (lastMouseClick == null) {
            lastMouseClick = new Coordinate(mousePositionInGameContext.getX(), mousePositionInGameContext.getY());
        }
        if (lastMouseClick.getX() != mousePositionInGameContext.getX() ||
                lastMouseClick.getY() != mousePositionInGameContext.getY()) {

            geometryItemsListIndex = 0;
            geometryItemsList = gameManager.get().pickGeometryItem(
                    new Coordinate(mousePositionInGameContext.getX(), -mousePositionInGameContext.getY()),
                    MOUSE_RECOGNIZE_DISTANCE / transformation.getScaleProperty().get());

            lastMouseClick = new Coordinate(mousePositionInGameContext.getX(), mousePositionInGameContext.getY());
        } else {
            geometryItemsListIndex = (geometryItemsListIndex + 1) % geometryItemsList.size();
        }
        log.info("received: " + geometryItemsListIndex + "/" + geometryItemsList.size());
        EventBusUtils.SELECTED_GEOMETRY_ITEMS_EVENT.trigger(geometryItemsList.get(geometryItemsListIndex));
    }

    /**
     * This is executed, when the mouse is pressed
     * and not actually released.
     * <p>
     * It will set {@link CanvasController#offsetBackup} and {@link CanvasController#dragStart}
     * relative to the mouse position.
     *
     * @param mouseEvent corresponding {@link MouseEvent}
     */
    public void onCanvasPressed(MouseEvent mouseEvent) {
        if (gameManager == null) {
            return;
        }
        offsetBackup = transformation.getOffsetProperty().get();
        dragStart = Vector2D.create(mouseEvent.getX(), mouseEvent.getY());
    }

    /**
     * This will be executed, when the mouse is pressed (and not released)
     * and moves over the canvas
     * <p>
     * It will swipe the game to the dragged position.
     *
     * @param mouseEvent corresponding {@link MouseEvent}
     */
    public void onCanvasDragged(MouseEvent mouseEvent) {
        dragged = true;
        if (gameManager == null) {
            return;
        }

        Vector2D dragOffset = Vector2D.create(mouseEvent.getX(), mouseEvent.getY()).subtract(dragStart);
        transformation.setOffset(dragOffset.add(offsetBackup));
    }

    public void onCanvasZoom(ScrollEvent scrollEvent) {
        if (gameManager == null) {
            return;
        }
        Vector2D mouse = new Vector2D(scrollEvent.getX(), scrollEvent.getY());
        final double scaleFactor = Math.exp(scrollEvent.getDeltaY() * 1e-2);
        transformation.scaleRelative(scaleFactor, mouse);
    }

    public void setGameManager(ObjectProperty<GameManager> gameManager) {
        this.gameManager = gameManager;
        gameManager.addListener(observable -> {
            if (this.gameManager.get() == null) {
                return;
            }
            this.gameManager.get().getViewIndex()
                    .addListener(observable1 -> drawShapes());
        });
    }
}