package com.treasure.hunt.strategy.geom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Geometry;

/**
 * Classifies a jts geometry item with parameters to distinguish between items for visualization/algorithm usages.
 *
 * @see GeometryType for further information about how to classifiy a geometry item.
 */

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class GeometryItem<T extends Geometry> {
    @NonNull T object;
    GeometryType type = new GeometryType();
}