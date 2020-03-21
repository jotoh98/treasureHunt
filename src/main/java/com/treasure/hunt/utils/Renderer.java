package com.treasure.hunt.utils;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.CanvasBoundary;
import com.treasure.hunt.jts.awt.PointTransformation;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.JavaFxDrawable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.jfree.fx.FXGraphics2D;

import java.awt.*;
import java.util.stream.Stream;

/**
 * The renderer responsible for drawing {@link GeometryItem}s on the {@link Canvas}.
 * It holds an {@link AdvancedShapeWriter} for enhanced shape writing.
 */
public class Renderer {

    /**
     * The jfree.fx graphics context to translate jts awt {@link Shape}s into
     * javafx {@link javafx.scene.shape.Shape}s.
     *
     * @see FXGraphics2D
     */
    private FXGraphics2D graphics2D;

    /**
     * Advanced shape writer.
     */
    private AdvancedShapeWriter shapeWriter;

    /**
     * Canvas to render on.
     */
    private Canvas canvas;


    /**
     * Construct the renderer held by {@link com.treasure.hunt.view.CanvasController}.
     * The given {@link GraphicsContext} forms the awt translator.
     * The {@link org.locationtech.jts.awt.ShapeWriter} gets linked to a new {@link CanvasBoundary}.
     *
     * @param canvas         the canvas to render on
     * @param transformation the point transformation for {@link org.locationtech.jts.geom.Geometry} instances
     */
    public Renderer(Canvas canvas, PointTransformation transformation) {
        this.canvas = canvas;
        this.shapeWriter = new AdvancedShapeWriter(transformation);
        graphics2D = new FXGraphics2D(this.canvas.getGraphicsContext2D());
        shapeWriter.setBoundary(new CanvasBoundary(this.canvas, transformation));
    }

    /**
     * Renders a list of filtered and sorted {@link GeometryItem}s.
     *
     * @param items item stream to be rendered
     */
    public void render(Stream<GeometryItem<?>> items) {
        clear();
        items.forEach(this::render);
    }


    /**
     * Render a single {@link GeometryItem}.
     *
     * @param item geometry item to be rendered
     */
    private void render(GeometryItem<?> item) {

        GeometryStyle geometryStyle = item.getGeometryStyle();

        if (!geometryStyle.isVisible()) {
            return;
        }

        Object object = item.getObject();

        if (object instanceof JavaFxDrawable) {
            render((JavaFxDrawable) object, geometryStyle);
        } else {
            render(object, geometryStyle);
        }

    }

    /**
     * Render an unspecified object with it's associated geometry style.
     *
     * @param object        object that may be rendered
     * @param geometryStyle the associated geometry style
     */
    private void render(Object object, GeometryStyle geometryStyle) {
        Shape shape = shapeWriter.toShape(object);

        if (geometryStyle.isFilled()) {
            graphics2D.setColor(geometryStyle.getFillColor());
            graphics2D.fill(shape);
        }

        graphics2D.setPaint(geometryStyle.getOutlineColor());
        graphics2D.setStroke(geometryStyle.getStroke());
        graphics2D.draw(shape);
    }

    /**
     * Render an {@link JavaFxDrawable} with it's associated geometry style.
     *
     * @param drawable the drawable to be rendered
     * @param style    the associated geometry style
     */
    private void render(JavaFxDrawable drawable, GeometryStyle style) {
        drawable.draw(style, canvas.getGraphicsContext2D(), shapeWriter);
    }

    /**
     * Clear up the canvas.
     */
    private void clear() {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
