package com.treasure.hunt.strategy.geom;

import org.locationtech.jts.geom.Geometry;

/**
 * Classifies a jts geometry item with parameters to distinguish between items for visualization/algorithm usages.
 *
 * @author hassel
 * @see GeometryType for further information about how to classifiy a geometry item.
 */
public class GeometryItem<T extends Geometry> {
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

    public GeometryType getGeometryType() {
        return this.geometryType;
    }

    public GeometryStyle getGeometryStyle() {
        return this.geometryStyle;
    }

    public T getObject() {
        return this.object;
    }
}