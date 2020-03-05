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
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

/**
 * @author axel12, dorianreineccius
 */
@Slf4j
public class CanvasController {
    /**
     * Determines, whether the mouse was dragged.
     */
    private boolean dragged = false;

    @Getter
    public Canvas canvas;
    public Pane canvasPane;
    private ObjectProperty<GameManager> gameManager = new SimpleObjectProperty<>();

    @Getter
    private PointTransformation transformation = new PointTransformation();
    private Renderer renderer;

    private Vector2D dragStart = new Vector2D();
    private Vector2D offsetBackup = new Vector2D();

    public void initialize() {
        makeCanvasResizable();
        renderer = new Renderer(canvas.getGraphicsContext2D(), transformation);

        renderer.addAdditional("grid", new GeometryItem<>(new Grid(), GeometryType.GRID));
        transformation.getScaleProperty().addListener(invalidation -> drawShapes());
        transformation.getOffsetProperty().addListener(invalidation -> drawShapes());

        //subscribeToGeometryItem();
    }

    public void makeCanvasResizable() {
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> drawShapes());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> drawShapes());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvas.widthProperty().bind(canvasPane.widthProperty());
    }

    /**
     * This method clears and draws all current {@link GeometryItem} given by the {@link GameManager} on the canvas.
     */
    void drawShapes() {
        Platform.runLater(() -> {
            if (gameManager == null) {
                throw new IllegalStateException("GameManager must not be null!");
            }
            if (gameManager.isNull().get()) {
                return;
            }
            renderer.render(gameManager.get());
        });
    }

    /**
     * Executed when the mouse was pressed and released.
     * TODO To much game logic in here!!11 maybe wrap into GameManager
     * Executing this will select a {@link GeometryItem} nearest to the mouse position.
     *
     * @param mouseEvent corresponding {@link MouseEvent}
     */
    public void onCanvasClicked(MouseEvent mouseEvent) {
        if (gameManager.isNull().get()) {
            return;
        }

        if (dragged) {
            dragged = false;
            return;
        }
        dragged = false;

        Coordinate c = transformation.revert(dragStart.toCoordinate());

        gameManager.get().refreshHighlighter(c, transformation.getScaleProperty().get());
        renderer.render(gameManager.get());
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
}