package com.treasure.hunt.strategy;

import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;

public abstract class Selectable {
    @Getter
    @Setter
    private boolean selected;

    /**
     * @return A {@link org.locationtech.jts.geom.Geometry}, on which we compare distances.
     */
    public abstract Geometry getGeometry();
}
