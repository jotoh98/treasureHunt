package com.treasure.hunt.view;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.geom.CircleHighlighter;
import com.treasure.hunt.geom.RectangleFixedHighlighter;
import com.treasure.hunt.geom.RectangleVariableHighlighter;
import com.treasure.hunt.jts.AdvancedShapeWriter;
import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.utils.JTSUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.jfree.fx.FXGraphics2D;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;

@Slf4j
/**
 * @author axel12, dorianreineccius
 */
public class CanvasController {
    /**
     * The maximum distance on canvas between the mouse and a {@link GeometryItem},
     * in which the mouse can select a {@link GeometryItem} on click.
     */
    public static final double MOUSE_RECOGNIZE_DISTANCE = 50;
    private GeometryItem selected;
    private GeometryItem highlighter;

    public Canvas canvas;
    public Pane canvasPane;
    private ObjectProperty<GameManager> gameManager;

    private PointTransformation transformation = new PointTransformation();
    private AdvancedShapeWriter shapeWriter = new AdvancedShapeWriter(transformation);

    private FXGraphics2D graphics2D;

    private Vector2D dragStart = new Vector2D();
    private Vector2D offsetBackup = new Vector2D();

    public void initialize() {
        makeCanvasResizable();
        graphics2D = new FXGraphics2D(canvas.getGraphicsContext2D());
    }

    public void makeCanvasResizable() {

        canvas.widthProperty().addListener((observableValue, number, t1) -> drawShapes());

        canvas.widthProperty().addListener((observableValue, number, t1) -> drawShapes());

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
                // TODO delete the following loop! (testing purposes)
                for (GeometryItem geometryItem : gameManager.get().getGeometryItems(true)) {
                    if (geometryItem.getGeometry() instanceof Point) {
                        GeometryItem greenCircle = new GeometryItem<>(
                                new CircleHighlighter(geometryItem.getGeometry().getCoordinate(),
                                        50, 64, JTSUtils.GEOMETRY_FACTORY),
                                GeometryType.STANDARD,
                                new GeometryStyle(true, Color.GREEN)
                        );
                        greenCircle.draw(graphics2D, shapeWriter);
                    }
                }
                // TODO not delete
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
     * This is executed, when the mouse is been clicked on the canvas.
     * It will select the nearest {@link GeometryItem} to the clicked position.
     *
     * @param mouseEvent corresponding {@link MouseEvent}
     */
    public void onCanvasClicked(MouseEvent mouseEvent) {
        if (gameManager == null) {
            return;
        }
        offsetBackup = transformation.getOffset();
        dragStart = Vector2D.create(mouseEvent.getX(), mouseEvent.getY());
        Vector2D mousePositionInGameContext = dragStart.subtract(offsetBackup);
        mousePositionInGameContext = mousePositionInGameContext.divide(transformation.getScale());
        GeometryItem geometryItem = gameManager.get().pickGeometryItem(
                new Coordinate(mousePositionInGameContext.getX(), -mousePositionInGameContext.getY()),
                MOUSE_RECOGNIZE_DISTANCE / transformation.getScale());
        if (geometryItem != null) {
            Geometry geometry = geometryItem.getGeometry();
            log.info("recognized: " + geometry); // TODO delete
            this.selected = geometryItem;
            if (geometryItem.getGeometry() instanceof Point) {
                this.highlighter = new GeometryItem(
                        new RectangleFixedHighlighter(
                                selected.getGeometry().getCoordinate(),
                                50, 50, JTSUtils.GEOMETRY_FACTORY),
                        GeometryType.STANDARD,
                        new GeometryStyle(true, Color.YELLOW));
            } else if (geometryItem.getGeometry() instanceof LineString) {
                double minX = geometryItem.getGeometry().getCoordinates()[0].x;
                double maxY = geometryItem.getGeometry().getCoordinates()[0].y;
                double maxX = geometryItem.getGeometry().getCoordinates()[0].x;
                double minY = geometryItem.getGeometry().getCoordinates()[0].y;
                for (Coordinate coord : geometryItem.getGeometry().getCoordinates()) {
                    if (coord.x < minX) {
                        minX = coord.x;
                    }
                    if (coord.x > maxX) {
                        maxX = coord.x;
                    }
                    if (coord.y < minY) {
                        minY = coord.y;
                    }
                    if (coord.y > minY) {
                        maxY = coord.y;
                    }
                }
                this.highlighter = new GeometryItem(
                        new RectangleVariableHighlighter(
                                new Coordinate(minX, maxY),
                                maxX - minX, maxY - minY,
                                JTSUtils.GEOMETRY_FACTORY),
                        GeometryType.STANDARD,
                        new GeometryStyle(true, Color.YELLOW));
                System.out.println(minX + ":" + minY + ":" + maxX + ":" + maxY);
                System.out.println("width: " + (maxX - minX) + " height: " + (maxY - minY));
            }
        }
    }

    public void onCanvasDragged(MouseEvent mouseEvent) {
        if (gameManager == null) {
            return;
        }
        Vector2D dragOffset = Vector2D.create(mouseEvent.getX(), mouseEvent.getY()).subtract(dragStart);
        transformation.setOffset(dragOffset.add(offsetBackup));
        drawShapes();
    }

    public void onCanvasZoom(ScrollEvent scrollEvent) {
        if (gameManager == null) {
            return;
        }
        Vector2D mouse = new Vector2D(scrollEvent.getX(), scrollEvent.getY());
        Vector2D direction = transformation.getOffset().subtract(mouse);

        double oldScale = transformation.getScale();
        double newScale = oldScale * Math.exp(scrollEvent.getDeltaY() * 1e-2);

        if (newScale > 0) {
            transformation.setScale(newScale);
            transformation.setOffset(mouse.add(direction.multiply(newScale / oldScale)));
            drawShapes();
        }
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