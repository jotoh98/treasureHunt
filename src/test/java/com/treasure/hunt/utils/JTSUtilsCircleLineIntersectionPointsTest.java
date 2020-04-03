package com.treasure.hunt.utils;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link JTSUtils#circleLineIntersectionPoints(Point, Point, Point, double)}.
 *
 * @author dorianreineccius
 */
public class JTSUtilsCircleLineIntersectionPointsTest {
    private Point pointm2u0 = JTSUtils.createPoint(-2, 0);
    private Point pointm1um1 = JTSUtils.createPoint(-1, -1);
    private Point pointm1u0 = JTSUtils.createPoint(-1, 0);
    private Point pointm1u1 = JTSUtils.createPoint(-1, 1);
    private Point pointm1u2 = JTSUtils.createPoint(-1, 2);
    private Point point0um1 = JTSUtils.createPoint(0, -1);
    private Point point0u0 = JTSUtils.createPoint(0, 0);
    private Point point0u1 = JTSUtils.createPoint(0, 1);
    private Point point1u0 = JTSUtils.createPoint(1, 0);
    private Point point1u1 = JTSUtils.createPoint(1, 1);
    private Point point1u2 = JTSUtils.createPoint(1, 2);
    private Point point2u0 = JTSUtils.createPoint(2, 0);
    private Point pointm9um9 = JTSUtils.createPoint(-9, -9);
    private Point pointm9u0 = JTSUtils.createPoint(-9, 0);
    private Point pointm9u10 = JTSUtils.createPoint(-9, 10);
    private Point pointm10u0 = JTSUtils.createPoint(-10, 0);

    @Test
    public void NoIntersectionPointTest() {
        assertTrue(JTSUtils.circleLineIntersectionPoints(pointm1u2, point1u2, point0u0, 1).size() == 0);
    }

    @Test
    public void OneIntersectionPointTest1() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(pointm1u1, point1u1, point0u0, 1);
        assertTrue(intersections.size() == 1);
        assertTrue(intersections.get(0).equalsExact(point0u1, 0));
    }

    /**
     * This is generalized version of a scenario, I found while testing the GameEngine.
     * Solution: Instead of [POINT (-4 0)], we got [POINT (-4 0.0000001), POINT (-4 -0.0000001)] as a consequence of an rounding error.
     * Instead of [POINT (-5 0)], we got [] as a consequence of an rounding error.
     */
    @Test
    public void generalBuggyScenario() {
        for (int i = 1; i < 10; i++) {
            Point p1 = JTSUtils.createPoint(-i, i + 1);
            Point p2 = JTSUtils.createPoint(-i, -i);
            Point treasure = JTSUtils.createPoint(-i - 1, 0);
            List<Point> intersections = JTSUtils.circleLineIntersectionPoints(
                    p1,
                    p2,
                    treasure, 1);
            System.out.println(intersections);
            if (intersections.size() > 0) {
                assertTrue("Failed with i=" + i + ", intersections: " + intersections + " does not contain (" + (-i) + ", " + (0) + ").",
                        intersections.get(0).equalsExact(JTSUtils.createPoint(-i, 0), 0.0000001) ||
                                intersections.get(1).equalsExact(JTSUtils.createPoint(-i, 0), 0.0000001));
            } else {
                LineSegment lineSegment = new LineSegment(p1.getCoordinate(), p2.getCoordinate());
                assertTrue(JTSUtils.createPoint(lineSegment.closestPoint(treasure.getCoordinate())).equalsExact(JTSUtils.createPoint(-i, 0), 0.0000001));
            }
        }
    }

    /**
     * Reduced version of {@link JTSUtilsCircleLineIntersectionPointsTest#generalBuggyScenario()} ()}
     */
    @Test
    public void reducedBuggyScenario() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(pointm1um1, pointm1u2, pointm2u0, 1);
        assertTrue(intersections.size() == 1);
        assertTrue(intersections.get(0).equalsExact(pointm1u0, 0));
    }

    @Test
    public void TwoIntersectionPointTest1() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(pointm1u0, point1u0, point0u0, 1);
        assertTrue(intersections.size() == 2);
        assertTrue(intersections.get(0).equalsExact(pointm1u0, 0.1) ||
                intersections.get(1).equalsExact(point1u0, 0.1));
        assertTrue(intersections.get(1).equalsExact(pointm1u0, 0.1) ||
                intersections.get(1).equalsExact(point1u0, 0.1));
    }

    @Test
    public void TwoIntersectionPointTest2() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(point0um1, point0u1, point0u0, 1);
        assertTrue(intersections.size() == 2);
        assertTrue(intersections.get(0).equalsExact(point0um1, 0.1) ||
                intersections.get(1).equalsExact(point0u1, 0.1));
        assertTrue(intersections.get(1).equalsExact(point0um1, 0.1) ||
                intersections.get(1).equalsExact(point0u1, 0.1));
    }

    /**
     * This test shows that the line  we gave is not handled like a {@link org.locationtech.jts.geom.LineSegment},
     * but handled like a infinite line.
     */
    @Test
    public void TwoIntersectionPointTest3() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(point0u0, point2u0, point0u0, 1);
        assertTrue(intersections.size() == 2);
        assertTrue(intersections.get(0).equalsExact(pointm1u0, 0) ||
                intersections.get(1).equalsExact(point1u0, 0));
        assertTrue(intersections.get(1).equalsExact(pointm1u0, 0) ||
                intersections.get(1).equalsExact(point1u0, 0));
    }

    @Test
    public void TwoIntersectionPointTest4() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(pointm1um1, point1u1, point0u0, 1);
        Point firstIntersection = JTSUtils.createPoint(-1 / Math.sqrt(2), -1 / Math.sqrt(2));
        Point secondIntersection = JTSUtils.createPoint(1 / Math.sqrt(2), 1 / Math.sqrt(2));
        assertTrue(intersections.size() == 2);
        assertTrue(intersections.get(0).equalsExact(firstIntersection, 0) ||
                intersections.get(1).equalsExact(secondIntersection, 0));
        assertTrue(intersections.get(1).equalsExact(firstIntersection, 0) ||
                intersections.get(1).equalsExact(secondIntersection, 0));
    }
}
