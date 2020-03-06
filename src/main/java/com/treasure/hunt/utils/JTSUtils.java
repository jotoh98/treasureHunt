package com.treasure.hunt.utils;

import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

import java.util.List;
import java.util.stream.Collectors;

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
     * @param x x-coordinate
     * @param y y-coordinate
     * @return {@link Point} lying on {@code (x,y)}.
     */
    public static Point createPoint(double x, double y) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(x, y));
    }

    /**
     * @param a the begin of the {@link LineString}.
     * @param b the end of the {@link LineString}.
     * @return A {@link LineString} containing only {@code a} and {@code b}.
     */
    public static LineString createLineString(Point a, Point b) {
        Coordinate[] coords = {a.getCoordinate(), b.getCoordinate()};
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
        if (intersection == null) {
            return null;
        }
        double distance = GEOMETRY_FACTORY.getPrecisionModel().makePrecise(segment.distance(intersection));
        if (distance != 0) {
            return null;
        }
        return intersection;
    }

    public static boolean doubleEqual(double a, double b) {
        return (0 == GEOMETRY_FACTORY.getPrecisionModel().makePrecise(a - b));

    }

    /**
     * @param angleHint where we want the middle point to go, from.
     * @return {@link Coordinate} going through the middle of the {@link AngleHint}
     */
    public static Coordinate middleOfAngleHint(AngleHint angleHint) {
        GeometryAngle angle = angleHint.getGeometry();
        return angle
                .rightVector()
                .rotate(angle.extend() / 2)
                .normalize()
                .translate(angle.getCenter());
    }

    /**
     * @param right  opening line of the angle
     * @param center of the angle
     * @param left   closing line of the angle
     * @return {@link Coordinate} going through the middle of the angle
     */
    public static Coordinate middleOfAngleHint(Coordinate right, Coordinate center, Coordinate left) {
        final GeometryAngle angle = new GeometryAngle(GEOMETRY_FACTORY, right, center, left);
        return angle
                .rightVector()
                .rotate(angle.extend() / 2)
                .normalize()
                .translate(angle.getCenter());
    }

    /**
     * @param from vector start
     * @param to   vector end
     * @return normalized {@link Vector2D} given by two {@link Coordinate}s {@code from} and {@code to}.
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
     * @return the {@link Coordinate} a given length-unit away from fixed {@link Coordinate} in vector direction
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
     * @param v0 first vector to check
     * @param v1 second vector to check
     * @return {@code true}, if both vectors {@code v0} and {@code v1} have coordinate-wise the same sign.
     * {@code false}, otherwise.
     */
    public static boolean signsEqual(Vector2D v0, Vector2D v1) {
        boolean xSignEqual = (v0.getX() > 0) == (v1.getX() > 0);
        boolean ySignEqual = (v0.getY() > 0) == (v1.getY() > 0);
        return xSignEqual && ySignEqual;
    }

    /**
     * @param vector vector to transform
     * @return new {@link Vector2D} with negated x-Coordinate of a {@code vector}.
     */
    public static Vector2D negateX(Vector2D vector) {
        return new Vector2D(-vector.getX(), vector.getY());
    }

    /**
     * @param vector vector to transform
     * @return new {@link Vector2D} with negated y-Coordinate of a {@code vector}.
     */
    public static Vector2D negateY(Vector2D vector) {
        return negateX(vector).negate();
    }

    /**
     * @param geometryAngle the view {@link GeometryAngle} the method looks upon searching the given point
     * @param coordinate    the {@link Coordinate}, we want to know, whether it lies in the {@code geometryAngle}.
     * @return {@code true}, if {@code coordinate} lies inside the given {@code geometryAngle}. {@code false}, otherwise.
     */
    public static boolean pointInAngle(GeometryAngle geometryAngle, Coordinate coordinate) {
        GeometryAngle treasureGeometryAngle = geometryAngle.copy();
        treasureGeometryAngle.setLeft(coordinate);
        double testExtend = treasureGeometryAngle.extend();
        return testExtend >= 0 && testExtend <= geometryAngle.extend();
    }

    /**
     * Tests, whether a given {@link Coordinate} lies inside the given angle,
     * which is defined by {@code right}, {@code center} and {@code left}.
     *
     * @param right      counter-clockwise, opening line of the angle.
     * @param center     center of the angle.
     * @param left       counter-clockwise, closing line of the angle.
     * @param coordinate the {@link Coordinate}, we want to know, whether it lies in the given angle.
     * @return {@code true}, if {@code coordinate} lies in the given angle. {@code false}, otherwise.
     */
    public static boolean pointInAngle(Coordinate right, Coordinate center, Coordinate left, Coordinate coordinate) {
        final GeometryAngle geometryAngle = new GeometryAngle(GEOMETRY_FACTORY, right, center, left);

        GeometryAngle treasureGeometryAngle = geometryAngle.copy();
        treasureGeometryAngle.setRight(coordinate);

        double testExtend = treasureGeometryAngle.extend();
        return testExtend >= 0 && testExtend <= geometryAngle.extend();
    }

    /**
     * @param searcher  the position of the {@link com.treasure.hunt.strategy.searcher.Searcher}.
     * @param treasure  the position of the treasure.
     * @param maxExtend number of {@code [0, 2 * Math.PI)} defining, how wide the angle is opened.
     * @return a valid {@link GeometryAngle}, randomly generated.
     */
    public static GeometryAngle validRandomAngle(Coordinate searcher, Coordinate treasure, double maxExtend) {
        if (maxExtend <= 0) {
            return null;
        }
        double givenAngle = Angle.angle(searcher, treasure);
        double extend = Math.random() * maxExtend;
        double start = givenAngle - extend * Math.random();
        return new GeometryAngle(GEOMETRY_FACTORY, searcher, start, extend);
    }

    public static List<Coordinate> getCoordinateList(List<? extends Geometry> geometries) {
        return geometries.stream()
                .map(Geometry::getCoordinate)
                .collect(Collectors.toList());
    }

    public static Polygon toPolygon(Envelope envelope) {
        return GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
                new Coordinate(envelope.getMinX(), envelope.getMinY()),
                new Coordinate(envelope.getMaxX(), envelope.getMinY()),
                new Coordinate(envelope.getMaxX(), envelope.getMaxY()),
                new Coordinate(envelope.getMinX(), envelope.getMaxY()),
                new Coordinate(envelope.getMinX(), envelope.getMinY()),
        });
    }
}
