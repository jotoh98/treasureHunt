package com.treasure.hunt.utils;

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
     * @param anglePointRight
     * @param angleCenter
     * @param anglePointLeft
     * @return {@link Coordinate} going through the middle of the angle with a distance of 1.
     */
    public static Coordinate middleOfAngleHint(Point anglePointRight, Point angleCenter, Point anglePointLeft) {
        return middleOfAngleHint(anglePointRight.getCoordinate(), angleCenter.getCoordinate(), anglePointLeft.getCoordinate());
    }

    /**
     * @param anglePointRight
     * @param angleCenter
     * @param anglePointLeft
     * @return {@link Coordinate} going through the middle of the angle with a distance of 1.
     */
    public static Coordinate middleOfAngleHint(Coordinate anglePointRight, Coordinate angleCenter, Coordinate anglePointLeft) {
        double betweenAngle = angleBetweenOriented(
                anglePointRight,
                angleCenter,
                anglePointLeft
        );

        double rightAngle = Angle.angle(angleCenter,
                anglePointRight);
        double resultAngle = Angle.normalizePositive(rightAngle + betweenAngle / 2);
        if (betweenAngle < 0) {
            resultAngle += Math.PI;
        }
        double x = angleCenter.getX() + (Math.cos(resultAngle));
        double y = angleCenter.getY() + (Math.sin(resultAngle));
        return new Coordinate(x, y);
    }

    /**
     * @param anglePointRight
     * @param anglePointCenter
     * @param anglePointLeft
     * @param point            the {@link Point}, we want to know, whether it lies in the angle
     * @return true, if {@code point} lies inside the given angle. false, otherwise
     */
    public static boolean pointInAngle(Point anglePointRight, Point anglePointCenter, Point anglePointLeft, Point point) {
        return pointInAngle(anglePointRight.getCoordinate(), anglePointCenter.getCoordinate(), anglePointLeft.getCoordinate(), point.getCoordinate());
    }

    /**
     * @param anglePointRight
     * @param anglePointCenter
     * @param anglePointLeft
     * @param coordinate       the {@link Coordinate}, we want to know, whether it lies in the angle
     * @return true, if {@code point} lies inside the given angle. false, otherwise
     */
    public static boolean pointInAngle(Coordinate anglePointRight, Coordinate anglePointCenter, Coordinate anglePointLeft, Coordinate coordinate) {
        double angle = angleBetweenOriented(anglePointRight, anglePointCenter, anglePointLeft);
        double angleHintToTreasure = angleBetweenOriented(anglePointRight, anglePointCenter, coordinate);
        return (angleHintToTreasure <= angle && 0 <= angleHintToTreasure);
    }

    /**
     * @param anglePointRight
     * @param anglePointCenter
     * @param anglePointLeft
     * @param radians          the allowed degree in radians, the angle may open
     * @return true, if the given angle is <= {@code radians}. false, otherwise
     */
    public static boolean angleDegreesSize(Point anglePointRight, Point anglePointCenter, Point anglePointLeft, double radians) {
        return angleDegreesSize(anglePointRight.getCoordinate(), anglePointCenter.getCoordinate(), anglePointLeft.getCoordinate(), radians);
    }

    /**
     * @param anglePointRight
     * @param anglePointCenter
     * @param anglePointLeft
     * @param radians          the allowed degree in radians, the angle may open
     * @return true, if the given angle is <= {@code radians}. false, otherwise
     */
    public static boolean angleDegreesSize(Coordinate anglePointRight, Coordinate anglePointCenter, Coordinate anglePointLeft, double radians) {
        double angle = angleBetweenOriented(anglePointRight, anglePointCenter, anglePointLeft);
        return (0 <= angle && angle <= radians);
    }
}
