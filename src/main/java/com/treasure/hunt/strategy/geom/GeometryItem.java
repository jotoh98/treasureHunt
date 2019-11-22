package com.treasure.hunt.strategy.geom;

import lombok.Getter;
import lombok.NonNull;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;

import java.awt.*;

/**
 * Classifies a jts geometry item with parameters to distinguish between items for visualization/algorithm usages.
 *
 * @see GeometryType for further information about how to classifiy a geometry item.
 */

@Getter
public class GeometryItem<T extends Geometry> {
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

    public Shape toShape(ShapeWriter shapeWriter) {
        return shapeWriter.toShape(object);
    }
}