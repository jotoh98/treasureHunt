package com.treasure.hunt.strategy;

import com.treasure.hunt.utils.JTSUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

@AllArgsConstructor
public class Treasure extends Selectable {
    @NonNull
    private final Coordinate coordinate;

    /**
     * {@inheritDoc}
     */
    public Geometry getGeometry() {
        return getPoint();
    }

    /**
     * @return A clone of the {@link Coordinate}, where the treasure lies.
     */
    public Coordinate getCoordinate() {
        return this.coordinate.copy();
    }

    /**
     * @return A new {@link Point}, where the treasure lies.
     */
    public Point getPoint() {
        return JTSUtils.createPoint(getCoordinate());
    }
}
