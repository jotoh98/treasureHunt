package com.treasure.hunt.view;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.awt.PointTransformation;
import com.treasure.hunt.jts.geom.Grid;
import com.treasure.hunt.jts.geom.HalfPlane;
import com.treasure.hunt.service.select.SelectionService;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.utils.EventBusUtils;
import com.treasure.hunt.utils.Renderer;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import lombok.Getter;
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
    private Renderer renderer;

    private Vector2D dragStart = new Vector2D();
    private Vector2D offsetBackup = new Vector2D();

    private BooleanProperty dragged = new SimpleBooleanProperty(false);

    public void initialize() {
        makeCanvasResizable();
        renderer = new Renderer(canvas, transformation);
        transformation.getScaleProperty().addListener(invalidation -> drawShapes());
        transformation.getOffsetProperty().addListener(invalidation -> drawShapes());
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
            renderer.render(gameManager.get().getVisibleGeometries());
        });
    }

    public void onCanvasClicked(MouseEvent mouseEvent) {
        if (gameManager.isNull().get()) {
            return;
        }
        offsetBackup = transformation.getOffsetProperty().get();
        dragStart = Vector2D.create(mouseEvent.getX(), mouseEvent.getY());

        dragged.set(false);
        EventBusUtils.INNER_POP_UP_EVENT_CLOSE.trigger();
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
        dragged.set(true);
    }

    /**
     * Handler for when the mouse press event on the canvas ends.
     * We determine that if if we didn't dragged the mouse moving the canvas rendering,
     * we can fire a click selection event.
     *
     * @param mouseEvent event of the lifted mouse position
     */
    public void onMouseLifted(MouseEvent mouseEvent) {
        if (!dragged.get()) {
            SelectionService.getInstance().handleClickEvent(mouseEvent, transformation, gameManager.get());
        }
        dragged.set(false);
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

            this.gameManager.get().addAdditional("grid", new GeometryItem<>(new Grid(), GeometryType.GRID));
            HalfPlane from = HalfPlane.from(Vector2D.create(1, 0), 1);
            this.gameManager.get().addAdditional("halfplane", new GeometryItem<>(from, GeometryType.HALF_PLANE));

            System.out.println(from.toString());

            this.gameManager.get().getAdditional()
                    .addListener((InvalidationListener) change -> drawShapes());
        });
    }
}
