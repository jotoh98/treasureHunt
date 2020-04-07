package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.awt.PointTransformation;
import com.treasure.hunt.service.settings.SettingsService;
import com.treasure.hunt.view.CanvasController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class NavigatorController {
    final DecimalFormat percentageFormatter = new DecimalFormat("##.##%");
    public Slider slider;
    public TextField textField;
    public VBox wrapper;
    private static final double SCALE_FACTOR = 1;
    public Canvas navigatorCanvas;

    private CanvasController canvasController;
    private Rectangle symbol = new Rectangle(0, 0, 0, 0);

    private SimpleObjectProperty<Point> searcherProperty = new SimpleObjectProperty<>();
    private SimpleObjectProperty<Point> treasureProperty = new SimpleObjectProperty<>();

    /**
     * Behaviour of user entering a scale.
     */
    public void onEnter() {
        double cleanScale;

        try {
            cleanScale = percentageFormatter.parse(textField.getText()).doubleValue();
        } catch (ParseException e) {
            try {
                cleanScale = Double.parseDouble(textField.getText().replace(",", "."));
            } catch (NumberFormatException e1) {
                cleanScale = canvasController.getTransformation().getScaleProperty().get();
            }
        }

        canvasController.getTransformation().setScale(cleanScale);
        wrapper.requestFocus();
        textField.setText(percentageFormatter.format(canvasController.getTransformation().getScaleProperty().get()));
    }

    /**
     * Bind the transformation properties to the slider and text field.
     *
     * @param gameManagerProperty the {@link ObjectProperty}, holding the {@link GameManager}
     * @param canvasController    {@link java.lang.ModuleLayer.Controller} holding the {@link PointTransformation}
     */
    public void init(ObjectProperty<GameManager> gameManagerProperty, CanvasController canvasController) {
        this.canvasController = canvasController;

        final ChangeListener<GameManager> managerInvalidator = (a, b, c) -> bindGameManager(gameManagerProperty.get());
        gameManagerProperty.addListener(managerInvalidator);
        managerInvalidator.changed(null, null, null);

        navigatorCanvasBindings();

        slider.setMax(PointTransformation.MAX_SCALE);
        slider.setMin(PointTransformation.MIN_SCALE);

        final PointTransformation transformer = canvasController.getTransformation();

        transformer
                .getScaleProperty()
                .addListener((observable, oldValue, newValue) ->
                        textField.setText(percentageFormatter.format(newValue)
                        ));

        slider.valueProperty().bindBidirectional(transformer.getScaleProperty());

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!slider.isPressed() || oldValue.equals(0)) {
                return;
            }
            final Canvas canvas = canvasController.getCanvas();
            final Vector2D center = Vector2D.create(canvas.getWidth(), canvas.getHeight()).divide(2);

            transformer.scaleOffset(((double) newValue) / ((double) oldValue), center);
        });
    }

    private void bindGameManager(final GameManager gameManager) {
        searcherProperty.bind(Bindings.createObjectBinding(
                () -> gameManager.getVisibleTurns().get(gameManager.getViewIndex().intValue()).getSearchPath().getLastPoint(),
                gameManager.getViewIndex()
        ));
        treasureProperty.bind(Bindings.createObjectBinding(
                () -> gameManager.getVisibleTurns().get(gameManager.getViewIndex().intValue()).getTreasureLocation(),
                gameManager.getViewIndex()
        ));
    }

    private void navigatorCanvasBindings() {
        final Canvas canvas = canvasController.getCanvas();
        final PointTransformation transformation = canvasController.getTransformation();
        navigatorCanvas.widthProperty().bind(wrapper.widthProperty().subtract(20));
        navigatorCanvas.heightProperty().bind(Bindings.min(200, navigatorCanvas.widthProperty()));

        symbol.widthProperty().bind(canvas.widthProperty()
                .divide(transformation.getScaleProperty())
                .divide(PointTransformation.INITIAL_SCALE)
                .multiply(SCALE_FACTOR)
        );
        symbol.heightProperty().bind(canvas.heightProperty()
                .divide(transformation.getScaleProperty())
                .divide(PointTransformation.INITIAL_SCALE)
                .multiply(SCALE_FACTOR)
        );
        symbol.xProperty().bind(Bindings.createDoubleBinding(
                () -> -transformation.getOffset().getX(),
                transformation.getOffsetProperty()
                )
                        .divide(transformation.getScaleProperty())
                        .divide(PointTransformation.INITIAL_SCALE)
                        .multiply(SCALE_FACTOR)
                        .add(navigatorCanvas.widthProperty().divide(2))
        );
        symbol.yProperty().bind(Bindings.createDoubleBinding(
                () -> -transformation.getOffset().getY(),
                transformation.getOffsetProperty()
                )
                        .divide(transformation.getScaleProperty())
                .divide(PointTransformation.INITIAL_SCALE)
                .multiply(SCALE_FACTOR)
                .add(navigatorCanvas.heightProperty().divide(2))
        );

        transformation.getOffsetProperty().addListener(observable -> drawNavigatorCanvas());
        canvas.widthProperty().addListener(observable -> drawNavigatorCanvas());
        transformation.getScaleProperty().addListener(observable -> drawNavigatorCanvas());
        wrapper.widthProperty().addListener(observable -> drawNavigatorCanvas());
        treasureProperty.addListener(observable -> drawNavigatorCanvas());
        searcherProperty.addListener(observable -> drawNavigatorCanvas());

        final AtomicReference<Vector2D> offset = new AtomicReference<>(null);
        final AtomicReference<Vector2D> dragStart = new AtomicReference<>(null);
        AtomicBoolean isOffsetAtomic = new AtomicBoolean(!SettingsService.getInstance().getSettings().isMiniMapDragged());

        navigatorCanvas.setOnMouseReleased(event -> {
            final Vector2D release = Vector2D.create(event.getX(), event.getY());
            boolean clicked = release.distance(dragStart.get()) < 1e-10;
            if (clicked) {
                setOffset(event.getX(), event.getY());
            }
        });

        navigatorCanvas.setOnMousePressed(event -> {
            isOffsetAtomic.set(!SettingsService.getInstance().getSettings().isMiniMapDragged());
            if (isOffsetAtomic.get()) {
                setOffset(event.getX(), event.getY());
                return;
            }
            offset.set(transformation.getOffset());
            dragStart.set(Vector2D.create(event.getX(), event.getY()));
        });

        navigatorCanvas.setOnMouseDragged(event -> {
            if (isOffsetAtomic.get()) {
                setOffset(event.getX(), event.getY());
                return;
            }
            if (offset.get() == null) {
                offset.set(transformation.getOffset());
            }
            if (dragStart.get() == null) {
                dragStart.set(Vector2D.create(event.getX(), event.getY()));
            }
            Vector2D dragOffset = Vector2D.create(event.getX(), event.getY()).subtract(dragStart.get());
            double scale = transformation.getScale();
            transformation.setOffset(offset.get().subtract(dragOffset.divide(SCALE_FACTOR).multiply(scale)));
        });

        navigatorCanvas.setOnScroll(event -> {
            final double scaleFactor = Math.exp(event.getDeltaY() * 1e-2);

            Vector2D vector2D;

            if (SettingsService.getInstance().getSettings().isMiniMapScrollCenter()) {
                vector2D = Vector2D.create(canvas.getWidth(), canvas.getHeight()).divide(2);
            } else {
                vector2D = Vector2D.create(event.getX(), event.getY())
                        .subtract(Vector2D.create(symbol.getX(), symbol.getY()))
                        .divide(SCALE_FACTOR)
                        .multiply(transformation.getScale());
            }

            transformation.scaleRelative(scaleFactor, vector2D);
        });
        drawNavigatorCanvas();
    }

    private Point2D transform(Point point) {
        final double x = navigatorCanvas.getWidth() / 2 + point.getX() * SCALE_FACTOR;
        final double y = navigatorCanvas.getHeight() / 2 - point.getY() * SCALE_FACTOR;
        return new Point2D(x, y);
    }

    private void setOffset(double x, double y) {
        final Vector2D middleOffset = Vector2D.create(navigatorCanvas.getWidth(), navigatorCanvas.getHeight()).divide(2);
        final Vector2D edge = Vector2D.create(symbol.getWidth(), symbol.getHeight()).divide(2);
        final Vector2D subtract = middleOffset.subtract(Vector2D.create(x, y));
        final double scale = canvasController.getTransformation().getScale();
        canvasController.getTransformation().setOffset(
                subtract.add(edge)
                        .divide(SCALE_FACTOR)
                        .multiply(scale)
        );
    }

    private void drawNavigatorCanvas() {
        final GraphicsContext context = navigatorCanvas.getGraphicsContext2D();
        double width = navigatorCanvas.getWidth();
        double height = navigatorCanvas.getHeight();

        context.clearRect(0, 0, width, height);

        context.setLineWidth(1);
        context.setStroke(Color.grayRgb(128));
        context.strokeLine(0, height / 2, width, height / 2);
        context.strokeLine(width / 2, 0, width / 2, height);

        if (treasureProperty.isNotNull().get()) {
            final Point2D treasure = transform(treasureProperty.get());
            context.setFill(Color.RED);
            context.fillOval(treasure.getX() - 1, treasure.getY() - 1, 3, 3);
        }
        if (searcherProperty.isNotNull().get()) {
            final Point2D treasure = transform(searcherProperty.get());
            context.setFill(Color.GREEN);
            context.fillOval(treasure.getX() - 1, treasure.getY() - 1, 3, 3);
        }

        context.setStroke(Color.WHITE);
        context.strokeRect(symbol.getX(), symbol.getY(), symbol.getWidth(), symbol.getHeight());

        context.setFill(Color.gray(1, .1));
        context.fillRect(symbol.getX(), symbol.getY(), symbol.getWidth(), symbol.getHeight());

        context.setStroke(Color.grayRgb(42));
        context.setLineWidth(2);
        context.strokeRect(0, 0, width, height);
    }
}
