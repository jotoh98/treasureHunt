package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * A type {@link Hint}, defining a circle, in which the treasure lies.
 *
 * @author dorianreineccius
 */
public class CircleHint extends Hint {
    private final Coordinate center;
    private final double radius;

    public CircleHint(Coordinate center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Circle getGeometry() {
        return new Circle(center, radius, JTSUtils.GEOMETRY_FACTORY);
    }

    /**
     * {@inheritDoc}
     */
    public List<GeometryItem<?>> getGeometryItems() {
        List<GeometryItem<?>> output = new ArrayList<>();
        output.add(new GeometryItem<>(
                this.getGeometry()
                , GeometryType.HINT_CENTER));
        return output;
    }

    public Point getCenter() {
        return JTSUtils.GEOMETRY_FACTORY.createPoint(this.center);
    }

    public double getRadius() {
        return this.radius;
    }
}
