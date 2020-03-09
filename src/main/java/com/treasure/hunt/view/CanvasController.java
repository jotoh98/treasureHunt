package com.treasure.hunt.view;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.awt.PointTransformation;
import com.treasure.hunt.jts.geom.Grid;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.utils.Renderer;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import lombok.Getter;
import org.locationtech.jts.math.Vector2D;

/**
 * The controller for the canvas and its behaviour.
 *
 * @author axel12, dorianreineccius
 */
public class CanvasController {
    /**
     * Drawing canvas.
     */
    @Getter
    public Canvas canvas;

    /**
     * Pane holding and resizing the canvas.
     */
    public Pane canvasPane;

    /**
     * Shared game manager instance.
     * Given and initialized by the {@link MainController}.
     */
    private ObjectProperty<GameManager> gameManager = new SimpleObjectProperty<>();

    /**
     * The point transformation used to manipulate how the canvas is rendered.
     */
    @Getter
    private PointTransformation transformation = new PointTransformation();

    /**
     * The renderer associated with the canvas.
     */
    private Renderer renderer;

    /**
     * Vector of the position at the beginning of a drag gesture.
     */
    private Vector2D dragStart = new Vector2D();

    /**
     * Distance vector of the drag offset to the current drag position.
     */
    private Vector2D offsetBackup = new Vector2D();

    /**
     * Initialize the canvas controller binding resizing and rendering.
     */
    public void initialize() {
        makeCanvasResizable();
        renderer = new Renderer(canvas, transformation);
        transformation.getScaleProperty().addListener(invalidation -> drawShapes());
        transformation.getOffsetProperty().addListener(invalidation -> drawShapes());
    }

    /**
     * Bind the canvas resizing to the rendering.
     */
    public void makeCanvasResizable() {
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> drawShapes());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> drawShapes());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvas.widthProperty().bind(canvasPane.widthProperty());
    }

    /**
     * Draw all the available shapes on the canvas.
     */
    public void drawShapes() {
        Platform.runLater(() -> {
            if (gameManager.isNull().get()) {
                return;
            }
            renderer.render(gameManager.get().getVisibleGeometries());
        });
    }

    /**
     * Click (and drag beginning) behaviour on the canvas.
     *
     * @param mouseEvent the click event
     */
    public void onCanvasClicked(MouseEvent mouseEvent) {
        if (gameManager.isNull().get()) {
            return;
        }
        offsetBackup = transformation.getOffsetProperty().get();
        dragStart = Vector2D.create(mouseEvent.getX(), mouseEvent.getY());
    }

    /**
     * This will be executed, when the mouse is pressed (and not released)
     * and moves over the canvas.
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

    /**
     * Zooming (scrolling) behaviour for the canvas.
     * It zooms the items on the canvas.
     *
     * @param scrollEvent mouse wheel scroll event
     */
    public void onCanvasZoom(ScrollEvent scrollEvent) {
        if (gameManager.isNull().get()) {
            return;
        }
        Vector2D mouse = new Vector2D(scrollEvent.getX(), scrollEvent.getY());
        final double scaleFactor = Math.exp(scrollEvent.getDeltaY() * 1e-2);
        transformation.scaleRelative(scaleFactor, mouse);
    }

    /**
     * Game manager property setter.
     *
     * @param gameManager the game manager property to set
     */
    public void setGameManager(ObjectProperty<GameManager> gameManager) {
        this.gameManager = gameManager;
        gameManager.addListener(observable -> {
            if (this.gameManager.isNull().get()) {
                return;
            }
            this.gameManager.get().getViewIndex()
                    .addListener(observable1 -> drawShapes());

            this.gameManager.get().addAdditional("grid", new GeometryItem<>(new Grid(), GeometryType.GRID));

        });
    }
}
