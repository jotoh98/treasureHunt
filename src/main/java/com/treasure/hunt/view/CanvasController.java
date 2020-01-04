package com.treasure.hunt.view;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.AdvancedShapeWriter;
import com.treasure.hunt.jts.PointTransformation;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import org.jfree.fx.FXGraphics2D;
import org.locationtech.jts.math.Vector2D;

/**
 * @author axel12
 */
public class CanvasController {
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
            }
        });
    }

    private void deleteShapes() {
        if (gameManager == null) {
            return;
        }
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void onCanvasClicked(MouseEvent mouseEvent) {
        if (gameManager == null) {
            return;
        }
        offsetBackup = transformation.getOffset();
        dragStart = Vector2D.create(mouseEvent.getX(), mouseEvent.getY());
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
