package com.treasure.hunt.utils;

import com.treasure.hunt.geom.GeometryAngle;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

public class JTSUtils {
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * Factory method to create a point with a shared {@link GeometryFactory}
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return {@link Point} for given {@link Double} coordinates
     */
    public static Point createPoint(double x, double y) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(x, y));
    }

    // TODO is this necessary?
    public static LineString createLineString(Point A, Point B) {
        Coordinate[] coords = {A.getCoordinate(), B.getCoordinate()};
        return GEOMETRY_FACTORY.createLineString(coords);
    }

    /**
     * Tests whether and infinite line intersects with a line segment
     *
     * @param infiniteLine infinite line
     * @param lineSegment  line between two points
     * @return intersection of infinite line and line segment
     */
    // TODO is this necessary?
    public static Point lineLineSegmentIntersection(LineSegment infiniteLine, LineSegment lineSegment) {
        Point intersection = GEOMETRY_FACTORY.createPoint(infiniteLine.lineIntersection(lineSegment));
        Point lineSegmentPointA = GEOMETRY_FACTORY.createPoint(lineSegment.p0);
        Point lineSegmentPointB = GEOMETRY_FACTORY.createPoint(lineSegment.p1);
        LineString lineSegString = createLineString(
                lineSegmentPointA,
                lineSegmentPointB
        );
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
        GeometryAngle angle = angleHint.getGeometryAngle();
        return angle
                .rightVector()
                .rotate(angle.extend() / 2)
                .normalize()
                .translate(angle.getCenter());
    }

    public static Coordinate middleOfAngleHint(Coordinate left, Coordinate center, Coordinate right) {
        final GeometryAngle angle = new GeometryAngle(GEOMETRY_FACTORY, center, left, right);
        return angle
                .rightVector()
                .rotate(angle.extend() / 2)
                .normalize()
                .translate(angle.getCenter());
    }

    /**
     * Utility to get a normalized {@link Vector2D} given by two {@link Coordinate}s.
     *
     * @param from vector start
     * @param to   vector end
     * @return normalized vector
     */
    public static Vector2D normalizedVector(Coordinate from, Coordinate to) {
        return Vector2D.create(from, to).normalize();
    }

    /**
     * Get the coordinate a given length-unit away from fixed {@link Coordinate} in vector (fixed to floating) direction.
     *
     * @param fixed    fixed relative coordinate
     * @param floating coordinate to provide direction vector
     * @param scale    length between fixed and asked coordinate
     * @return coordinate a given length-unit away from fixed {@link Coordinate} in vector direction
     */
    public static Coordinate coordinateInDistance(Coordinate fixed, Coordinate floating, double scale) {
        return normalizedVector(fixed, floating).multiply(scale).translate(fixed);
    }

    /**
     * Get the coordinate 1 length-unit away from fixed {@link Coordinate} in vector (fixed to floating) direction.
     *
     * @param fixed    fixed relative coordinate
     * @param floating coordinate to provide direction vector
     * @return coordinate 1 length-unit away from fixed {@link Coordinate} in vector direction
     */
    public static Coordinate normalizedCoordinate(Coordinate fixed, Coordinate floating) {
        return coordinateInDistance(fixed, floating, 1.0);
    }

    /**
     * Proof, that the x- or y-coordinates of two vectors have the same sign.
     *
     * @param v0 first vector to check
     * @param v1 second vector to check
     * @return whether both vectors have coordinate-wise the same sign
     */
    public static boolean signsEqual(Vector2D v0, Vector2D v1) {
        boolean xSignEqual = (v0.getX() > 0) == (v1.getX() > 0);
        boolean ySignEqual = (v0.getY() > 0) == (v1.getY() > 0);
        return xSignEqual && ySignEqual;
    }

    /**
     * Get a new {@link Vector2D} with negated x-Coordinate of a {@link Vector2D}.
     *
     * @param v vector to transform
     * @return vector with negated x-coordinate
     */
    public static Vector2D negateX(Vector2D v) {
        return new Vector2D(-v.getX(), v.getY());
    }

    /**
     * Get a new {@link Vector2D} with negated y-Coordinate of a {@link Vector2D}.
     *
     * @param v vector to transform
     * @return vector with negated y-coordinate
     */
    public static Vector2D negateY(Vector2D v) {
        return negateX(v).negate();
    }

    /**
     * Test, if a given coordinate lays inside of the viewing angle given by a {@link GeometryAngle}.
     *
     * @param geometryAngle the view {@link GeometryAngle} the method looks upon searching the given point
     * @param coordinate    the {@link Coordinate}, we want to know, whether it lies in the angle
     * @return true, if {@code point} lies inside the given angle. false, otherwise
     */
    public static boolean pointInAngle(GeometryAngle geometryAngle, Coordinate coordinate) {
        GeometryAngle treasureGeometryAngle = geometryAngle.copy();
        treasureGeometryAngle.setLeft(coordinate);
        double testExtend = treasureGeometryAngle.extend();
        return testExtend >= 0 && testExtend <= geometryAngle.extend();
    }

    public static boolean pointInAngle(Coordinate center, Coordinate left, Coordinate right, Coordinate coordinate) {
        final GeometryAngle geometryAngle = new GeometryAngle(GEOMETRY_FACTORY, center, left, right);
        GeometryAngle treasureGeometryAngle = geometryAngle.copy();
        treasureGeometryAngle.setLeft(coordinate);
        double testExtend = treasureGeometryAngle.extend();
        return testExtend >= 0 && testExtend <= geometryAngle.extend();
    }

    public static Vector2D lineVector(LineSegment lineSegment) {
        return new Vector2D(lineSegment.p0, lineSegment.p1);
    }
}
