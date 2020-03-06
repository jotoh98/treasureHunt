package com.treasure.hunt.strategy;

import com.treasure.hunt.utils.JTSUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

@AllArgsConstructor
public class Treasure extends Selectable {
    @NonNull
    private final Point point;

    /**
     * @return The {@link Coordinate} of the treasure.
     */
    public Coordinate getCoordinate() {
        return new Coordinate(point.getX(), point.getY());
    }

    public Point getPoint() {
        return JTSUtils.createPoint(point.getX(), point.getY());
    }
}
