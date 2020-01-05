package com.treasure.hunt.utils;

import com.treasure.hunt.geom.GeometryAngle;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

/**
 * A utility class for the work with {@link org.locationtech.jts}.
 *
 * @author Rank, dorianreineccius, jotoh, axel12
 */
public final class JTSUtils {
    /**
     * A static final shared {@link GeometryFactory} we use, such that every usage
     * uses the same settings of the geometry factory.
     */
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(1000000000));

    private JTSUtils() {
    }

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
     * Tests whether the line line intersects with the linesegment segment and returns the intersecting Coordinate
     * (if one exists).
     *
     * @param line    a {@link LineSegment}
     * @param segment a {@link LineSegment}
     * @return an intersection {@link Point} of the {@link LineSegment} objects {@code line} and {@code lineSegment}
     */
    public static Coordinate lineWayIntersection(LineSegment line, LineSegment segment) {
        Coordinate intersection = line.lineIntersection(segment);
        if (intersection == null)
            return null;
        double distance = GEOMETRY_FACTORY.getPrecisionModel().makePrecise(segment.distance(intersection));
        if (distance != 0)
            return null;
        return intersection;
    }

    public static boolean doubleEqual(double a, double b) {
        return (0 == GEOMETRY_FACTORY.getPrecisionModel().makePrecise(a - b));

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

    public static Coordinate middleOfAngleHint(Coordinate right, Coordinate center, Coordinate left) {
        final GeometryAngle angle = new GeometryAngle(GEOMETRY_FACTORY, right, center, left);
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
     * Proofs, that the x- or y-coordinates of two vectors have the same sign.
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
     * Tests, whether a given coordinate lays inside of the viewing angle given by a {@link GeometryAngle}.
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

    public static boolean pointInAngle(Coordinate right, Coordinate center, Coordinate left, Coordinate coordinate) {
        final GeometryAngle geometryAngle = new GeometryAngle(GEOMETRY_FACTORY, right, center, left);

        GeometryAngle treasureGeometryAngle = geometryAngle.copy();
        treasureGeometryAngle.setRight(coordinate);

        double testExtend = treasureGeometryAngle.extend();
        return testExtend >= 0 && testExtend <= geometryAngle.extend();
    }

    public static Vector2D lineVector(LineSegment lineSegment) {
        return new Vector2D(lineSegment.p0, lineSegment.p1);
    }

    public static GeometryAngle validRandomAngle(Coordinate searcher, Coordinate treasure, double maxExtend) {
        if (maxExtend <= 0) {
            return null;
        }
        double givenAngle = Angle.angle(searcher, treasure);
        double extend = Math.random() * maxExtend;
        double start = givenAngle - extend * Math.random();
        return new GeometryAngle(GEOMETRY_FACTORY, searcher, start, extend);
    }
}
