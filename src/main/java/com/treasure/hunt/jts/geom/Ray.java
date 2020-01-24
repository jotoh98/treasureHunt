package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.Shapeable;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;

/**
 * @author jotoh
 */
public class Ray extends LineSegment implements Shapeable {
    /**
     * The constructor.
     *
     * @param p0 the first {@link Coordinate} of this ray.
     * @param p1 the second {@link Coordinate} of this ray.
     */
    public Ray(Coordinate p0, Coordinate p1) {
        super(p0, p1);
        this.p1 = JTSUtils.normalizedCoordinate(p0, p1);
    }

    boolean interSectionInRay(Coordinate intersection) {
        Vector2D rayVector = new Vector2D(p0, p1);
        Vector2D testVector = new Vector2D(p0, intersection);
        return JTSUtils.signsEqual(rayVector, testVector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Coordinate intersection(LineSegment line) {
        Coordinate intersection = lineIntersection(line);

        if (intersection == null) {
            return null;
        } else if (interSectionInRay(intersection)) {
            return intersection;
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLength() {
        return Double.POSITIVE_INFINITY;
    }

    /**
     * @param shapeWriter the {@link org.locationtech.jts.awt.ShapeWriter} we use, to convert {@code this} into a {@link Shape}.
     * @return A shape, {@code this} is converted into.
     */
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        double diameter = shapeWriter.getPointTransformation().diameter();
        Coordinate end = JTSUtils.coordinateInDistance(p0, p1, diameter);
        return shapeWriter.createLine(p0, end);
    }
}
