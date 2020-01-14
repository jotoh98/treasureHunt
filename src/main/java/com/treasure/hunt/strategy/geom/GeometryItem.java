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

    /**
     * The constructor.
     *
     * @param object        the {@link org.locationtech.jts.geom.Geometry} or {@link com.treasure.hunt.jts.awt.Shapeable}.
     * @param geometryType  the {@link GeometryType}, defining its role.
     * @param geometryStyle the {@link GeometryStyle}, defining its looking.
     */
    public GeometryItem(T object, GeometryType geometryType, GeometryStyle geometryStyle) {
        assert (object != null);
        this.object = object;
        this.geometryType = geometryType;
        this.geometryStyle = geometryStyle;
    }

    /**
     * The constructor, using default {@link GeometryStyle}.
     *
     * @param object       the {@link org.locationtech.jts.geom.Geometry} or {@link com.treasure.hunt.jts.awt.Shapeable}.
     * @param geometryType the {@link GeometryType}, defining its role.
     */
    public GeometryItem(T object, GeometryType geometryType) {
        this(object, geometryType, GeometryStyle.getDefaults(geometryType));
    }

    /**
     * This lets {@code this} convert to a {@link Shape} via the given {@code shapeWriter}
     * and draws itself on the given {@code graphics2D}.
     *
     * @param graphics2D  where we want to draw {@code this} on.
     * @param shapeWriter converting {@code this} into a {@link Shape}.
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