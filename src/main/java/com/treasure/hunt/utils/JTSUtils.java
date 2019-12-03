package com.treasure.hunt.utils;

import com.treasure.hunt.strategy.hint.impl.AngleHint;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

import static org.locationtech.jts.algorithm.Angle.angleBetweenOriented;

public class JTSUtils {
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public static Point createPoint(double x, double y) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(x, y));
    }

    // TODO is this necessary?
    public static LineString createLineString(Point A, Point B) {
        Coordinate[] coords = {A.getCoordinate(), B.getCoordinate()};
        return GEOMETRY_FACTORY.createLineString(coords);
    }

    /**
     * Tests whether line line intersects with the linesegment linesegment
     *
     * @param line
     * @param lineSegment
     * @return
     */
    // TODO is this necessary?
    public static Point lineLineSegmentIntersection(LineSegment line, LineSegment lineSegment) {
        Point intersection = GEOMETRY_FACTORY.createPoint(line.lineIntersection(lineSegment));
        LineString lineSegString = createLineString(GEOMETRY_FACTORY.createPoint(lineSegment.p0),
                GEOMETRY_FACTORY.createPoint(lineSegment.p1));
        if (lineSegString.contains(intersection)) {
            return intersection;
        }
        return null;
    }

    /**
     * @param angleHint where we want the middle point to go, from.
     * @return {@link Point} going through the middle of the {@link AngleHint}
     */
    public static Coordinate middleOfAngleHint(AngleHint angleHint) {
        double betweenAngle = angleBetweenOriented(
                angleHint.getGeometryAngle().getRight(),
                angleHint.getGeometryAngle().getCenter(),
                angleHint.getGeometryAngle().getLeft()
        );

        double rightAngle = Angle.angle(angleHint.getGeometryAngle().getCenter(),
                angleHint.getGeometryAngle().getRight());
        double resultAngle = Angle.normalizePositive(rightAngle + betweenAngle / 2);
        if (betweenAngle < 0) {
            resultAngle += Math.PI;
        }
        double x = angleHint.getGeometryAngle().getCenter().getX() + (Math.cos(resultAngle));
        double y = angleHint.getGeometryAngle().getCenter().getY() + (Math.sin(resultAngle));
        return new Coordinate(x, y);
    }

    public static Vector2D normalizedVector(Coordinate from, Coordinate to) {
        return Vector2D.create(from, to).normalize();
    }

    public static Coordinate coordinateInDistance(Coordinate fixed, Coordinate floating, double scale) {
        return normalizedVector(fixed, floating).multiply(scale).translate(fixed);
    }

    public static Coordinate normalizedCoordinate(Coordinate fixed, Coordinate floating) {
        return coordinateInDistance(fixed, floating, 1.0);
    }

    public static boolean signsEqual(Vector2D v0, Vector2D v1) {
        boolean xSignEqual = (v0.getX() > 0) == (v1.getX() > 0);
        boolean ySignEqual = (v0.getY() > 0) == (v1.getY() > 0);
        return xSignEqual && ySignEqual;
    }


    public static Vector2D negateX(Vector2D v) {
        return new Vector2D(-v.getX(), v.getY());
    }

    public static Vector2D negateY(Vector2D v) {
        return negateX(v).negate();
    }
}
