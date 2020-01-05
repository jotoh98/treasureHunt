package com.treasure.hunt.strategy.geom;

import com.treasure.hunt.jts.AdvancedShapeWriter;
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
public class GeometryItem<T extends Geometry> {
    @NonNull
    @Getter
    T geometry;
    GeometryType geometryType;
    GeometryStyle geometryStyle;

    public GeometryItem(T geometry, GeometryType geometryType, GeometryStyle geometryStyle) {
        assert (geometry != null);
        this.geometry = geometry;
        this.geometryType = geometryType;
        this.geometryStyle = geometryStyle;
    }

    public GeometryItem(T geometry) {
        this(geometry, GeometryType.STANDARD, GeometryStyle.getDefaults(GeometryType.STANDARD));
    }

    public GeometryItem(T geometry, GeometryType geometryType) {
        this(geometry, geometryType, GeometryStyle.getDefaults(geometryType));
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
        Shape shape = shapeWriter.toShape(geometry);
        if (geometryStyle.isFilled()) {
            graphics2D.setColor(geometryStyle.getFillColor());
            graphics2D.fill(shape);
        }

        graphics2D.setPaint(geometryStyle.getOutlineColor());
        graphics2D.setStroke(geometryStyle.getStroke());
        graphics2D.draw(shape);
    }
}