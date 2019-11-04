package com.treasure.hunt.strategy.geom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.locationtech.jts.geom.Geometry;

/**
 * Classifies a jts geometry item with parameters to distinguish between items for visualization/algorithm usages.
 *
 * @see GeometryType for further information about how to classifiy a geometry item.
 */

@AllArgsConstructor
@Value
public class GeometryItem<T extends Geometry> {
    @NonNull
    @Getter
    T object;

    GeometryType type;
    GeometryStyle style;

    public GeometryItem(T object) {
        this(object, GeometryType.STANDARD, GeometryStyle.getDefaults(GeometryType.STANDARD));
    }

    public GeometryItem(T object, GeometryType type) {
        this(object, type, GeometryStyle.getDefaults(type));
    }
}