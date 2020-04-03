package com.treasure.hunt.utils;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link JTSUtils#circleLineIntersectionPoints(Point, Point, Point, double)}.
 *
 * @author dorianreineccius
 */
public class JTSUtilsCircleLineIntersectionPointsTest {
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
}
