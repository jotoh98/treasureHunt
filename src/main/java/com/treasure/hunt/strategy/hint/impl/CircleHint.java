package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import lombok.Getter;
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

    @Getter
    private Circle circle;

    public CircleHint(Point center, double radius) {
        this(center.getCoordinate(), radius);
    }

    public CircleHint(Coordinate center, double radius) {
        this(new Circle(center, radius));
    }

    public CircleHint(Circle circle) {
        this.circle = circle;
    }

    /**
     * {@inheritDoc}
     */
    public List<GeometryItem<?>> getGeometryItems() {
        List<GeometryItem<?>> output = new ArrayList<>();
        output.add(new GeometryItem<>(circle, GeometryType.HINT_CIRCLE));
        return output;
    }
}
