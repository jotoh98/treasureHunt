package com.treasure.hunt.view;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.CanvasBoundary;
import com.treasure.hunt.jts.awt.PointTransformation;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import lombok.Getter;
import org.jfree.fx.FXGraphics2D;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

/**
 * @author axel12, dorianreineccius
 */
public class CanvasController {
    @Getter
    public Canvas canvas;
    public Pane canvasPane;
    private ObjectProperty<GameManager> gameManager = new SimpleObjectProperty<>();

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
        shapeWriter.setBoundary(new CanvasBoundary(canvas, transformation));
    }

    public void makeCanvasResizable() {
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> drawShapes());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> drawShapes());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvas.widthProperty().bind(canvasPane.widthProperty());
    }

    public void drawShapes() {
        Platform.runLater(() -> {
            if (gameManager.isNull().get()) {
                return;
            }
            deleteShapes();
            gameManager.get().getGeometryItems(true)
                    .forEach(geometryItem -> geometryItem.draw(graphics2D, shapeWriter));
        });
    }

    private void deleteShapes() {
        if (gameManager.isNull().get()) {
            return;
        }
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void onCanvasClicked(MouseEvent mouseEvent) {
        if (gameManager.isNull().get()) {
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
        if (gameManager.isNull().get()) {
            return;
        }
        Vector2D dragOffset = Vector2D.create(mouseEvent.getX(), mouseEvent.getY()).subtract(dragStart);
        transformation.setOffset(dragOffset.add(offsetBackup));
    }

    public void onCanvasZoom(ScrollEvent scrollEvent) {
        if (gameManager.isNull().get()) {
            return;
        }
        Vector2D mouse = new Vector2D(scrollEvent.getX(), scrollEvent.getY());
        final double scaleFactor = Math.exp(scrollEvent.getDeltaY() * 1e-2);
        transformation.scaleRelative(scaleFactor, mouse);
    }

    public void setGameManager(ObjectProperty<GameManager> gameManager) {
        this.gameManager = gameManager;
        gameManager.addListener(observable -> {
            if (this.gameManager.isNull().get()) {
                return;
            }
            this.gameManager.get().getViewIndex()
                    .addListener(observable1 -> drawShapes());

        });
    }

    public Coordinate upperLeftBoundary() {
        return transformation.revert(0, 0);
    }

    public Coordinate lowerRightBoundary() {
        return transformation.revert(canvas.getWidth(), canvas.getHeight());
    }
}
