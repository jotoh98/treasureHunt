package com.treasure.hunt.utils;

import com.treasure.hunt.jts.awt.CanvasBoundary;
import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.StrategyFromPaper;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A utility class for the work with {@link org.locationtech.jts}.
 *
 * @author Rank, dorianreineccius, jotoh, axel12
 */
@Slf4j
public final class JTSUtils {
    /**
     * A static final shared {@link GeometryFactory} we use, such that every usage
     * uses the same settings of the geometry factory.
     */
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(1000000));
    public static final PrecisionModel APPROXIMATELY_PRECISION = new PrecisionModel(10000);

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

    public static Point createPoint(Coordinate p) {
        return createPoint(p.x, p.y);
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
        if (doubleEqualApproximately(line.distancePerpendicular(segment.p0), 0)) {
            return segment.p0;
        }
        if (doubleEqualApproximately(line.distancePerpendicular(segment.p1), 0)) {
            return segment.p1;
        }

        Coordinate intersection = line.lineIntersection(segment);
        if (intersection == null) {
            return null;
        }
        if (!doubleEqual(segment.distance(intersection), 0)) {
            return null;
        }
        return intersection;
    }

    /**
     * Tests whether the convex polygon polygon intersects with the line line and returns the intersecting coordinates
     * if it does.
     *
     * @param polygon a {@link Polygon}
     * @param line    a {@link LineSegment}
     * @return an ArrayList of the intersecting coordinates if they exist, a empty ArrayList otherwise
     */
    public static ArrayList<Coordinate> convexPolygonLineIntersection(Polygon polygon, LineSegment line) {
        ArrayList<Coordinate> result = new ArrayList<>();
        Coordinate[] coordinatesPolygon = polygon.getCoordinates();
        for (int i = 0; i < coordinatesPolygon.length - 1 && result.size() < 3; i++) {
            Coordinate intersection = lineWayIntersection(line, new LineSegment(coordinatesPolygon[i], coordinatesPolygon[i + 1]));
            if (intersection != null) {
                result.add(intersection);
            }
        }
        return result;
    }

    public static boolean doubleEqual(double a, double b) {
        return (0 == GEOMETRY_FACTORY.getPrecisionModel().makePrecise(a - b));
    }

    public static boolean coordinateEqual(Coordinate a, Coordinate b) {
        return doubleEqual(a.x, b.x) && doubleEqual(a.y, b.y);
    }

    public static boolean doubleEqualApproximately(double a, double b) {
        return (0 == APPROXIMATELY_PRECISION.makePrecise(a - b));
    }

    /**
     * @param angleHint where we want the middle point to go, from.
     * @return {@link Coordinate} going through the middle of the {@link AngleHint}
     */
    public static Coordinate middleOfAngleHint(AngleHint angleHint) {
        GeometryAngle angle = angleHint.getGeometryAngle();
        return middleOfGeometryAngle(angle);
    }

    public static Coordinate middleOfGeometryAngle(GeometryAngle angle) {
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
        return validRandomAngle(searcher, treasure, maxExtend, 0);
    }

    public static GeometryAngle validRandomAngle(Coordinate searcher, Coordinate treasure, double maxExtend, double minExtend) {

        if (maxExtend <= 0 || minExtend < 0 || minExtend >= maxExtend) {
            return null;
        }

        double givenAngle = Angle.angle(searcher, treasure);
        double extend = minExtend + Math.random() * (maxExtend - minExtend);
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

    /**
     * Get the intersections between the infinite line and the visual boundary.
     *
     * @param boundary boundary supplying the border {@link LineSegment}s
     * @param infinite infinite line
     * @return the intersections between the infinite line extension and the boundary {@link LineSegment}s
     */
    public static List<Coordinate> getBoundaryIntersections(CanvasBoundary boundary, LineSegment infinite) {
        final ArrayList<Coordinate> intersections = new ArrayList<>();
        boundary.toLineSegments().forEach(boundarySegment -> {
            final Coordinate intersection = infinite.intersection(boundarySegment);
            if (intersection != null) {
                intersections.add(intersection);
            }
        });
        return intersections;
    }

    /**
     * Shuffles a new circle covered by the boundary circle, containing the pivot coordinate and obtaining the given radius;
     *
     * @param boundary circle to cover the generated circle
     * @param pivot    coordinate, that must lay inside of the generated circle
     * @param radius   radius of the generated circle
     * @return a valid generated circle
     */
    public static Circle randomContainedCircle(Circle boundary, Coordinate pivot, double radius) {
        assert boundary.inside(pivot);
        assert boundary.getRadius() >= radius;
        final Circle pivotBoundary = new Circle(pivot, radius);
        final Circle innerBoundary = new Circle(boundary.getCenter(), boundary.getRadius() - radius);

        Coordinate center;
        if (innerBoundary.covers(pivotBoundary)) {
            center = randomInCircle(pivotBoundary);
        } else {
            Circle generator = innerBoundary;
            Circle tester = pivotBoundary;
            if (2 * radius < boundary.getRadius()) {
                generator = pivotBoundary;
                tester = innerBoundary;
            }
            do {
                center = randomInCircle(generator);
            } while (!tester.inside(center));
        }
        return new Circle(center, radius);
    }

    /**
     * Get the {@link ConvexHull} for a list of {@link Coordinate}s.
     *
     * @param coordinates the list of coordinates
     * @return the convex hull for the list of coordinates
     */
    public static ConvexHull createConvexHull(List<Coordinate> coordinates) {
        return new ConvexHull(
                coordinates.toArray(Coordinate[]::new),
                JTSUtils.GEOMETRY_FACTORY
        );
    }

    /**
     * Generates a treasure location according to set preferences.
     *
     * @return treasure point
     */
    public static Point shuffleTreasure() {
        Optional<Number> fixedDistance = PreferenceService.getInstance()
                .getPreference(PreferenceService.TREASURE_DISTANCE);

        if (fixedDistance.isPresent()) {
            Coordinate treasure = Vector2D.create(fixedDistance.get().doubleValue(), 0).rotate(2 * Math.PI * Math.random()).translate(new Coordinate());
            return JTSUtils.GEOMETRY_FACTORY.createPoint(treasure);
        }

        double maxDistance = PreferenceService.getInstance()
                .getPreference(PreferenceService.MAX_TREASURE_DISTANCE, 100)
                .doubleValue();
        double minDistance = PreferenceService.getInstance()
                .getPreference(PreferenceService.MIN_TREASURE_DISTANCE, 0)
                .doubleValue();

        Coordinate treasure = Vector2D.create(Math.random() * (maxDistance - minDistance) + minDistance, 0).rotate(2 * Math.PI * Math.random()).translate(new Coordinate());
        return JTSUtils.GEOMETRY_FACTORY.createPoint(treasure);
    }

    public static boolean isApproximatelyOnLine(Coordinate point, LineSegment line) {
        return APPROXIMATELY_PRECISION.makePrecise((point.x - line.p0.x) / (line.p1.x - line.p0.x) - (point.y - line.p0.y) / (line.p1.y - line.p0.y)) == 0;
    }

    /**
     * Generate a random coordinate in the given circle.
     *
     * @param circle circle to cover the generated coordinate
     * @return random coordinate in given circle
     */
    public static Coordinate randomInCircle(Circle circle) {
        return Vector2D.create(circle.getRadius(), 0)
                .rotate(Math.random() * 2 * Math.PI)
                .multiply(Math.random())
                .translate(circle.getCenter());
    }

    /**
     * this function can be called to determine if the specified hint is a bad Hint
     * defined be the paper in the context of the specified rectangle
     *
     * @param rectangle the rectangle as polygon
     * @param hint      the hint
     * @return
     */
    public static boolean isBadHint(Polygon rectangle, AngleHint hint) {
        if (!(hint instanceof HalfPlaneHint)) {
            log.debug("can't be a bad hint,, only HalfPlaneHints can be bad hints");
            EventBusUtils.LOG_LABEL_EVENT.trigger("Supplied hint is not a halfplane: Are you playing with a HalfPlaneHint hider?");
            return false;
        }

        if (rectangle == null || !rectangle.isRectangle()) {
            EventBusUtils.LOG_LABEL_EVENT.trigger("Supplied polyon is not a rectangle: Are you playing against StrategyFromPaper?");
            log.debug("can't be a bad hint, specified polygon is not a rectangle");
            return false;
        }

        Coordinate[] rectangleCoordinates = rectangle.getCoordinates();
        for (Coordinate c : rectangleCoordinates) {
            log.trace("coord" + c);
        }
        Coordinate centroid = GeometricUtils.centerOfRectangle(rectangleCoordinates);
        log.trace("centroid" + centroid);
        log.trace("player" + hint.getGeometryAngle().getCenter());
        if (!centroid.equals2D(hint.getGeometryAngle().getCenter())) {
            log.debug("can't be a bad hint, player is not in center of current rectangle");
            return false;
        }

        Coordinate topLeft = rectangleCoordinates[0];
        Coordinate bottomLeft = rectangleCoordinates[3];
        Coordinate topRight = rectangleCoordinates[1];

        // since a halfPlane - rectangle cut through the centroid is point symmetrical, only 2 adjacent edges need to be checked
        // one of them has the intersection, sometimes both if the Line goes on the diagonal of the rectangle
        LineSegment top = new LineSegment(topLeft, topRight);
        LineSegment left = new LineSegment(bottomLeft, topLeft);
        LineSegment hintLineSegment = new LineSegment(hint.getGeometryAngle().getCenter(), hint.getGeometryAngle().getRight());

        double length_y = 1; // distance y from paper paper (page 5)

        Coordinate topIntersect = top.lineIntersection(hintLineSegment);
        log.trace("intersect with top " + topIntersect);

        if (topIntersect != null) { // in case of parallel

            // topleft
            if (topIntersect.x >= topLeft.x && topIntersect.x <= topLeft.x + length_y) {
                log.debug("bad hint: top edge, left side");
                return true;
            }
            // top right
            if (topIntersect.x <= topRight.x && topIntersect.x >= topRight.x - length_y) {
                log.debug("bad hint: top edge, right side");
                return true;
            }
        }

        Coordinate leftIntersect = left.lineIntersection(hintLineSegment);
        log.trace("intersect with left " + leftIntersect);

        if (leftIntersect != null) {// in case of parallel

            // left top
            if (leftIntersect.y >= topLeft.y - length_y && leftIntersect.y <= topLeft.y) {
                log.debug("bad hint:  left edge, top side");
                return true;
            }

            // left bottom
            if (leftIntersect.y >= bottomLeft.y && leftIntersect.y <= bottomLeft.y + length_y) {
                log.debug("bad hint:  left edge, bottom side");
                return true;
            }
        }
        log.debug("good hint");
        return false;
    }

    /**
     * Calculates the intersection {@link Point}s, from a line and a circle.
     * The line is described as going from {@code pointA} to {@code pointB} and
     * the circle ist described by having his center on {@code center} and a radius of {@code radius}.
     * <p>
     * This code is copied from "https://stackoverflow.com/questions/13053061/circle-line-intersection-points".
     *
     * @param pointA the first {@link Point} of the line
     * @param pointB the second {@link Point} of the line
     * @param center the center {@link Point} of the circle
     * @param radius the radius of the circle
     * @return a list, containing 0, 1 or 2 {@link Point}s, representing the intersections of the line and the circle.
     */
    public static List<Point> circleLineIntersectionPoints(Point pointA, Point pointB, Point center, double radius) {
        double baX = pointB.getX() - pointA.getX();
        double baY = pointB.getY() - pointA.getY();
        double caX = center.getX() - pointA.getX();
        double caY = center.getY() - pointA.getY();

        double a = Math.pow(baX, 2) + Math.pow(baY, 2);

        double pBy2 = (baX * caX + baY * caY) / a;
        double q = (Math.pow(caX, 2) + Math.pow(caY, 2) - Math.pow(radius, 2)) / a;

        double disc = Math.pow(pBy2, 2) - q;
        if (disc < 0) {
            return Collections.emptyList();
        }
        // if disc == 0 ... dealt with later
        double tmpSqrt = Math.sqrt(disc);
        double abScalingFactor1 = -pBy2 + tmpSqrt;
        double abScalingFactor2 = -pBy2 - tmpSqrt;

        Point p1 = createPoint(pointA.getX() - baX * abScalingFactor1,
                pointA.getY() - baY * abScalingFactor1);
        if (disc == 0) { // abScalingFactor1 == abScalingFactor2
            return Collections.singletonList(p1);
        }
        Point p2 = createPoint(pointA.getX() - baX * abScalingFactor2,
                pointA.getY() - baY * abScalingFactor2);
        return Arrays.asList(p1, p2);
    }
}
