package com.treasure.hunt.strategy.geom;

import lombok.*;
import org.locationtech.jts.geom.Geometry;

/**
 * Classifies a jts geometry item with parameters to distinguish between items for visualization/algorithm usages.
 *
 * @see GeometryType for further information about how to classifiy a geometry item.
 */

@AllArgsConstructor
@RequiredArgsConstructor
public class GeometryItem<T extends Geometry> {
    @Getter
    @Setter
    @NonNull T object;
    @Getter
    @Setter
    GeometryType type = new GeometryType();
}