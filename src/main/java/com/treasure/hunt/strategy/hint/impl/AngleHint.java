package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dorianreineccius, jotoh
 */
@Slf4j
public class AngleHint extends Hint {
    protected Coordinate right;
    protected Coordinate center;
    protected Coordinate left;

    public AngleHint(Coordinate right, Coordinate center, Coordinate left) {
        this.right = right;
        this.center = center;
        this.left = left;
    }

    public AngleHint(GeometryAngle angle) {
        this(angle.getRight(), angle.getCenter(), angle.getLeft());
    }

    /**
     * @return A copy of the {@link Coordinate} of right.
     */
    public Coordinate getRight() {
        return this.right.copy();
    }

    /**
     * @return A copy of the {@link Coordinate} of center.
     */
    public Coordinate getCenter() {
        return this.center.copy();
    }

    /**
     * @return A copy of the {@link Coordinate} of left.
     */
    public Coordinate getLeft() {
        return this.left.copy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeometryAngle getGeometry() {
        return new GeometryAngle(JTSUtils.GEOMETRY_FACTORY, getRight(), getCenter(), getLeft());
    }

    public List<GeometryItem<?>> getGeometryItems() {
        List<GeometryItem<?>> output = new ArrayList<>();
        output.add(new GeometryItem<>(this.getGeometry(), GeometryType.HINT_ANGLE));
        return output;
    }
}
