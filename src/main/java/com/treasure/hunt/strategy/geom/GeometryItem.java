package com.treasure.hunt.strategy.geom;

import lombok.Value;
import org.locationtech.jts.geom.Geometry;

/**
 * Classifies a jts geometry item with parameters to distinguish between items for visualization/algorithm usages.
 *
 * @see GeometryType for further information about how to classifiy a geometry item.
 */
@Value
public class GeometryItem<T extends Geometry> {
    T object;
    GeometryType type;
}
