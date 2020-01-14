package com.treasure.hunt.strategy.geom;


import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.jfree.fx.FXGraphics2D;

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

    public GeometryItem(T object) {
        this(object, GeometryType.STANDARD);
    }

    public void draw(FXGraphics2D graphics2D, AdvancedShapeWriter shapeWriter) {
        if (!geometryStyle.isVisible()) {
            return;
        }
        Shape shape = shapeWriter.toShape(object);

        if (shape == null) {
            return;
        }

        if (geometryStyle.isFilled()) {
            graphics2D.setColor(geometryStyle.getFillColor());
            graphics2D.fill(shape);
        }

        graphics2D.setPaint(geometryStyle.getOutlineColor());
        graphics2D.setStroke(geometryStyle.getStroke());
        graphics2D.draw(shape);
    }
}