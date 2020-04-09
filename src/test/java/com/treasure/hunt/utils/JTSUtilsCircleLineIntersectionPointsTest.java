package com.treasure.hunt.utils;

import javafx.util.Pair;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link JTSUtils#circleLineIntersectionPoints(Point, Point, Point, double)}.
 *
 * @author Dorian Reineccius
 */
public class JTSUtilsCircleLineIntersectionPointsTest {
    private Point pointm2um1 = JTSUtils.createPoint(-2, -1);
    private Point pointm2u0 = JTSUtils.createPoint(-2, 0);
    private Point pointm2u1 = JTSUtils.createPoint(-2, 1);
    private Point pointm1um2 = JTSUtils.createPoint(-1, -2);
    private Point pointm1um1 = JTSUtils.createPoint(-1, -1);
    private Point pointm1u0 = JTSUtils.createPoint(-1, 0);
    private Point pointm1u1 = JTSUtils.createPoint(-1, 1);
    private Point pointm1u2 = JTSUtils.createPoint(-1, 2);
    private Point point0um2 = JTSUtils.createPoint(0, -2);
    private Point point0um1 = JTSUtils.createPoint(0, -1);
    private Point point0u0 = JTSUtils.createPoint(0, 0);
    private Point point0u1 = JTSUtils.createPoint(0, 1);
    private Point point1um2 = JTSUtils.createPoint(1, -2);
    private Point point1um1 = JTSUtils.createPoint(1, -1);
    private Point point1u0 = JTSUtils.createPoint(1, 0);
    private Point point1u1 = JTSUtils.createPoint(1, 1);
    private Point point1u2 = JTSUtils.createPoint(1, 2);
    private Point point2um1 = JTSUtils.createPoint(2, -1);
    private Point point2u0 = JTSUtils.createPoint(2, 0);
    private Point point2u1 = JTSUtils.createPoint(2, 1);
    private Point pointm9um9 = JTSUtils.createPoint(-9, -9);
    private Point pointm9u0 = JTSUtils.createPoint(-9, 0);
    private Point pointm9u10 = JTSUtils.createPoint(-9, 10);
    private Point pointm10u0 = JTSUtils.createPoint(-10, 0);

    /**
     * Tests {@link JTSUtils#circleLineIntersectionPoints(Point, Point, Point, double)}
     * with no intersections expected.
     */
    @Test
    public void NoIntersectionPointTest() {
        assertTrue(JTSUtils.circleLineIntersectionPoints(pointm1u2, point1u2, point0u0, 1).size() == 0);
        assertTrue(JTSUtils.circleLineIntersectionPoints(pointm1um2, point1um2, point0u0, 1).size() == 0);
        assertTrue(JTSUtils.circleLineIntersectionPoints(pointm2u1, pointm2um1, point0u0, 1).size() == 0);
        assertTrue(JTSUtils.circleLineIntersectionPoints(point2u1, point2um1, point0u0, 1).size() == 0);

        assertTrue(JTSUtils.circleLineIntersectionPoints(pointm2u0, point0um2, point0u0, 1).size() == 0);
        assertTrue(JTSUtils.circleLineIntersectionPoints(point0um2, point2u0, point0u0, 1).size() == 0);
        assertTrue(JTSUtils.circleLineIntersectionPoints(point2u0, point0um2, point0u0, 1).size() == 0);
        assertTrue(JTSUtils.circleLineIntersectionPoints(point0um2, pointm2u0, point0u0, 1).size() == 0);
    }

    /**
     * Tests {@link JTSUtils#circleLineIntersectionPoints(Point, Point, Point, double)}
     * with one intersections expected.
     */
    @Test
    public void OneIntersectionPointTest1() {
        assertContains(JTSUtils.circleLineIntersectionPoints(pointm1u1, point1u1, point0u0, 1), point0u1);
        assertContains(JTSUtils.circleLineIntersectionPoints(pointm1um1, pointm1u1, point0u0, 1), pointm1u0);
        assertContains(JTSUtils.circleLineIntersectionPoints(pointm1um1, point1um1, point0u0, 1), point0um1);
        assertContains(JTSUtils.circleLineIntersectionPoints(point1um1, point1u1, point0u0, 1), point1u0);
        double magic = Math.sqrt(2);
        Point a = JTSUtils.createPoint(-magic, 0);
        Point b = JTSUtils.createPoint(0, magic);
        Point c = JTSUtils.createPoint(magic, 0);
        Point d = JTSUtils.createPoint(0, -magic);
        Point ab = JTSUtils.createPoint(-magic / 2, magic / 2);
        Point bc = JTSUtils.createPoint(magic / 2, magic / 2);
        Point cd = JTSUtils.createPoint(magic / 2, -magic / 2);
        Point da = JTSUtils.createPoint(-magic / 2, -magic / 2);
        assertContains(JTSUtils.circleLineIntersectionPoints(a, b, point0u0, 1), ab);
        assertContains(JTSUtils.circleLineIntersectionPoints(b, c, point0u0, 1), bc);
        assertContains(JTSUtils.circleLineIntersectionPoints(c, d, point0u0, 1), cd);
        assertContains(JTSUtils.circleLineIntersectionPoints(d, a, point0u0, 1), da);
    }

    /**
     * Tests {@link JTSUtils#circleLineIntersectionPoints(Point, Point, Point, double)}
     * with horizontal lines.
     */
    @Test
    public void TwoIntersectionPointTest1() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(pointm1u0, point1u0, point0u0, 1);
        assertContains(intersections, pointm1u0);
        assertContains(intersections, point1u0);
    }

    /**
     * Tests {@link JTSUtils#circleLineIntersectionPoints(Point, Point, Point, double)}
     * with vertical lines.
     */
    @Test
    public void TwoIntersectionPointTest2() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(point0um1, point0u1, point0u0, 1);
        assertContains(intersections, point0um1);
        assertContains(intersections, point0u1);
    }

    /**
     * This test shows that the line we gave is not handled like a {@link org.locationtech.jts.geom.LineSegment},
     * but handled like a infinite line.
     */
    @Test
    public void TwoIntersectionPointTest3() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(point0u0, point2u0, point0u0, 1);
        assertContains(intersections, pointm1u0);
        assertContains(intersections, point1u0);
    }

    /**
     * Tests {@link JTSUtils#circleLineIntersectionPoints(Point, Point, Point, double)}
     * with diagonal lines.
     */
    @Test
    public void TwoIntersectionPointTest4() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(pointm1um1, point1u1, point0u0, 1);
        double magic = 1 / Math.sqrt(2);
        Point firstIntersection = JTSUtils.createPoint(-magic, -magic);
        Point secondIntersection = JTSUtils.createPoint(magic, magic);
        assertContains(intersections, firstIntersection);
        assertContains(intersections, secondIntersection);
        intersections = JTSUtils.circleLineIntersectionPoints(pointm1u1, point1um1, point0u0, 1);
        firstIntersection = JTSUtils.createPoint(-magic, magic);
        secondIntersection = JTSUtils.createPoint(magic, -magic);
        assertContains(intersections, firstIntersection);
        assertContains(intersections, secondIntersection);
    }

    /**
     * This is a buggy scenario, I found while testing the {@link com.treasure.hunt.game.GameEngine}.
     */
    @Test
    public void buggyScenario() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(pointm9um9, pointm9u10, pointm10u0, 1);
        assertContains(intersections, pointm9u0);
    }

    /**
     * Reduced version of {@link JTSUtilsCircleLineIntersectionPointsTest#generalBuggyScenario()}
     */
    @Test
    public void reducedBuggyScenario() {
        List<Point> intersections = JTSUtils.circleLineIntersectionPoints(pointm1um1, pointm1u2, pointm2u0, 1);
        assertContains(intersections, pointm1u0);
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
     * Tests {@link JTSUtils#getLinearEquation(Point, Point)}
     */
    @Test
    public void getLinearEquationTest() {
        assertEqualsLinearEquation(JTSUtils.getLinearEquation(point0u0, point1u0), 0, 0, 0);
        assertEqualsLinearEquation(JTSUtils.getLinearEquation(point0u0, point1u1), 1, 0, 0);
        assertEqualsLinearEquation(JTSUtils.getLinearEquation(point0u0, point2u1), 0.5, 0, 0);
        assertEqualsLinearEquation(JTSUtils.getLinearEquation(point0u0, point1u2), 2, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> JTSUtils.getLinearEquation(point0u0, point0u0));
        assertThrows(IllegalArgumentException.class, () -> JTSUtils.getLinearEquation(point0u0, point0u1));
        assertThrows(IllegalArgumentException.class, () -> JTSUtils.getLinearEquation(point0u0, point0um1));
        assertEqualsLinearEquation(JTSUtils.getLinearEquation(point1u1, point2u0), -1, 2, 0);
    }

    /**
     * Tests {@link JTSUtils#getOrthogonal(double, Point)}
     */
    @Test
    public void getOrthogonalTest() {
        assertEqualsLinearEquation(JTSUtils.getOrthogonal(1, point0u0), -1, 0, 0);
        assertEqualsLinearEquation(JTSUtils.getOrthogonal(0.5, point0u0), -2, 0, 0);
        assertEqualsLinearEquation(JTSUtils.getOrthogonal(2, point0u0), -0.5, 0, 0);
        assertEqualsLinearEquation(JTSUtils.getOrthogonal(1, point0u1), -1, 1, 0);
        assertEqualsLinearEquation(JTSUtils.getOrthogonal(-1, point0u1), 1, -1, 0);
    }

    /**
     * Tests, whether the {@code function} {@code y = xm + b} equals {@code y = x*expectedM + expectedB}
     *
     * @param function  the actual function {@code y = xm + b}, we want to compare with {@code expectedM} and {@code expectedB}
     * @param expectedM the expected {@code m}
     * @param expectedB the expected {@code b}
     * @param tolerance the error, we allow between two doubles, to be equal nevertheless
     * @throws AssertionError if the {@code function} {@code y = xm + b} does not equals {@code y = x*expectedM + expectedB}
     */
    private void assertEqualsLinearEquation(Pair<Double, Double> function, double expectedM, double expectedB, double tolerance) {
        double differenceM = Math.abs(function.getKey() - expectedM);
        assertTrue("Expected m=" + function.getKey() + " equals " + expectedM + " with a tolerance of " + tolerance +
                " but the difference was actually " + differenceM, differenceM <= tolerance);
        double differenceB = Math.abs(function.getKey() - expectedM);
        assertTrue("Expected b=" + function.getValue() + " equals " + expectedB + " with a tolerance of " + tolerance +
                " but the difference was actually " + differenceB, differenceB <= tolerance);
    }

    /**
     * Tests, whether {@code pointList} contains {@code point}
     *
     * @param pointList the list we want to test, whether it contains {@code point}
     * @param point     the {@link Point} we want to test, whether it is contained in {@code pointList}
     * @throws AssertionError if {@code pointList} does not contain {@code point}
     */
    private void assertContains(List<Point> pointList, Point point) {
        for (Point p : pointList) {
            if (p.equalsExact(point, 0)) {
                return;
            }
        }
        assertTrue("pointList " + pointList + " does not contain " + point + " as expected.", false);
    }
}
