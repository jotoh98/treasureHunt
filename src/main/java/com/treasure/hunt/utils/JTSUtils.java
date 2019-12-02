package com.treasure.hunt.utils;

import com.treasure.hunt.strategy.hint.impl.AngleHint;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.*;

import static org.locationtech.jts.algorithm.Angle.angleBetweenOriented;

/**
 * This is a helper class, containing helper methods for
 * calculations on JTS objects.
 *
 * @author Rank, dorianreineccius
 */
public class JTSUtils {
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * @param x coordinate.
     * @param y coordinate.
     * @return a {@link Point} defined by (x,y)
     */
    public static Point createPoint(double x, double y) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(x, y));
    }

    /**
     * @param A a point
     * @param B a point
     * @return a {@link LineString} containing only {@code A} and {@code B}
     */
    public static LineString createLineString(Point A, Point B) {
        Coordinate[] coords = {A.getCoordinate(), B.getCoordinate()};
        return GEOMETRY_FACTORY.createLineString(coords);
    }

    /**
     * TODO is this necessary ?
     *
     * @param line        a {@link LineSegment}
     * @param lineSegment a {@link LineSegment}
     * @return an intersection {@link Point} of the {@link LineSegment} objects {@code line} and {@code lineSegment}
     */
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
                angleHint.getAnglePointRight().getCoordinate(),
                angleHint.getCenter().getCoordinate(),
                angleHint.getAnglePointLeft().getCoordinate()
        );

        double rightAngle = Angle.angle(angleHint.getCenter().getCoordinate(),
                angleHint.getAnglePointRight().getCoordinate());
        double resultAngle = Angle.normalizePositive(rightAngle + betweenAngle / 2);
        if (betweenAngle < 0) {
            resultAngle += Math.PI;
        }
        double x = angleHint.getCenter().getX() + (Math.cos(resultAngle));
        double y = angleHint.getCenter().getY() + (Math.sin(resultAngle));
        return new Coordinate(x, y);
    }
}
