package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * A type {@link Hint}, defining a circle, in which the treasure lies.
 *
 * @author dorianreineccius
 */
public class CircleHint extends Hint {
    private Point center;
    private double radius;

    public CircleHint(Point center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    /**
     * {@inheritDoc}
     */
    public List<GeometryItem<?>> getGeometryItems() {
        List<GeometryItem<?>> output = new ArrayList<>();
        output.add(new GeometryItem<>(
                new Circle(center.getCoordinate(), radius, JTSUtils.GEOMETRY_FACTORY)
                , GeometryType.HINT_CENTER));
        return output;
    }

    public Point getCenter() {
        return this.center;
    }

    public double getRadius() {
        return this.radius;
    }
}
