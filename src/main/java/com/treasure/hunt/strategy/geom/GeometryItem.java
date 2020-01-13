package com.treasure.hunt.strategy.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.jfree.fx.FXGraphics2D;
import org.locationtech.jts.geom.Geometry;

import java.awt.*;

/**
 * Classifies a jts geometry item with parameters to distinguish between items for visualization/algorithm usages.
 *
 * @author jotoh, dorianreineccius
 * @see GeometryType for further information about how to classifiy a geometry item.
 */
@Getter
@EqualsAndHashCode(of = {"object", "geometryType"})
public class GeometryItem<T> {
    @NonNull
    @Getter
    private T object;
    @NonNull
    private GeometryType geometryType;
    private GeometryStyle geometryStyle;

    public GeometryItem(T object, GeometryType geometryType, GeometryStyle geometryStyle) {
        assert (object != null);
        this.object = object;
        this.geometryType = geometryType;
        this.geometryStyle = geometryStyle;
    }

    public GeometryItem(T object, GeometryType geometryType) {
        this(object, geometryType, GeometryStyle.getDefaults(geometryType));
    }

    /**
     * This draws {@code this} GeometryItem on the {@code graphics2D}.
     *
     * @param graphics2D  the {@link Graphics2D} to draw {@code this} on.
     * @param shapeWriter the {@link org.locationtech.jts.awt.ShapeWriter} converting the {@link Geometry} of {@code this} to a {@link Shape}.
     */
    public void draw(FXGraphics2D graphics2D, AdvancedShapeWriter shapeWriter) {
        if (!geometryStyle.isVisible()) {
            return;
        }
        Shape shape = shapeWriter.toShape(object);
        if (geometryStyle.isFilled()) {
            graphics2D.setColor(geometryStyle.getFillColor());
            graphics2D.fill(shape);
        }

        graphics2D.setPaint(geometryStyle.getOutlineColor());
        graphics2D.setStroke(geometryStyle.getStroke());
        graphics2D.draw(shape);
    }
}