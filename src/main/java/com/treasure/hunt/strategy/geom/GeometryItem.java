package com.treasure.hunt.strategy.geom;

import com.treasure.hunt.jts.AdvancedShapeWriter;
import lombok.Getter;
import lombok.NonNull;

import java.awt.*;

/**
 * Classifies a jts geometry item with parameters to distinguish between items for visualization/algorithm usages.
 *
 * @see GeometryType for further information about how to classifiy a geometry item.
 */

@Getter
public class GeometryItem<T> {
    @NonNull
    @Getter
    T object;
    GeometryType geometryType;
    GeometryStyle geometryStyle;

    public GeometryItem(T object, GeometryType geometryType, GeometryStyle geometryStyle) {
        assert (object != null);
        this.object = object;
        this.geometryType = geometryType;
        this.geometryStyle = geometryStyle;
    }

    public GeometryItem(T object) {
        this(object, GeometryType.STANDARD, GeometryStyle.getDefaults(GeometryType.STANDARD));
    }

    public GeometryItem(T object, GeometryType geometryType) {
        this(object, geometryType, GeometryStyle.getDefaults(geometryType));
    }

    public void draw(Graphics2D graphics2D, AdvancedShapeWriter shapeWriter) {
        if (!geometryStyle.isVisible()) {
            return;
        }

        Shape shape = shapeWriter.toShape(object);

        if (geometryStyle.isFilled()) {
            graphics2D.setPaint(geometryStyle.getFillColor());
            graphics2D.fill(shape);
        }
        graphics2D.setPaint(geometryStyle.getOutlineColor());
        graphics2D.setStroke(geometryStyle.toStroke());
        graphics2D.draw(shape);
    }
}