package com.treasure.hunt.utils;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testbench, testing {@link JTSUtils#pointInAngle(Coordinate, Coordinate, Coordinate, Coordinate)}.
 *
 * @author Dorian Reineccius
 */
class JTSUtilsPointInAngleTest {

    /**
     * Tests for {@link JTSUtils#pointInAngle(Coordinate, Coordinate, Coordinate, Coordinate)},
     * testing, whether given points lie in the upper right quadrant.
     */
    @Test
    public void pointInAngle1() {
        assertTrue(inUpperRightQuadrant(new Coordinate(0, 0)));
        assertTrue(inUpperRightQuadrant(new Coordinate(0, 1)));
        assertTrue(inUpperRightQuadrant(new Coordinate(1, 0)));
        assertTrue(inUpperRightQuadrant(new Coordinate(1, 1)));
        assertTrue(inUpperRightQuadrant(new Coordinate(76, 23)));

        assertFalse(inUpperRightQuadrant(new Coordinate(-1, 0)));
        assertFalse(inUpperRightQuadrant(new Coordinate(0, -1)));
        assertFalse(inUpperRightQuadrant(new Coordinate(-1, -1)));
        assertFalse(inUpperRightQuadrant(new Coordinate(-54, -23)));
    }

    /**
     * Tests for {@link JTSUtils#pointInAngle(Coordinate, Coordinate, Coordinate, Coordinate)},
     * testing, whether given points lie in the upper half plane.
     */
    @Test
    public void pointInAngle2() {
        assertTrue(inUpperHalfPlane(new Coordinate(0, 0)));
        assertTrue(inUpperHalfPlane(new Coordinate(0, 1)));
        assertTrue(inUpperHalfPlane(new Coordinate(1, 0)));
        assertTrue(inUpperHalfPlane(new Coordinate(1, 1)));
        assertTrue(inUpperHalfPlane(new Coordinate(-1, 0)));
        assertTrue(inUpperHalfPlane(new Coordinate(-1, 1)));
        assertTrue(inUpperHalfPlane(new Coordinate(12, 34)));

        assertFalse(inUpperHalfPlane(new Coordinate(0, -1)));
        assertFalse(inUpperHalfPlane(new Coordinate(1, -1)));
        assertFalse(inUpperHalfPlane(new Coordinate(-1, -1)));
        assertFalse(inUpperHalfPlane(new Coordinate(12, -34)));
    }

    /**
     * Tests for {@link JTSUtils#pointInAngle(Coordinate, Coordinate, Coordinate, Coordinate)},
     * testing, whether given points lie in the lower half plane.
     */
    @Test
    public void pointInAngle3() {
        assertTrue(inLowerHalfPlane(new Coordinate(0, 0)));
        assertTrue(inLowerHalfPlane(new Coordinate(0, -1)));
        assertTrue(inLowerHalfPlane(new Coordinate(1, 0)));
        assertTrue(inLowerHalfPlane(new Coordinate(1, -1)));
        assertTrue(inLowerHalfPlane(new Coordinate(-1, 0)));
        assertTrue(inLowerHalfPlane(new Coordinate(-1, -1)));
        assertTrue(inLowerHalfPlane(new Coordinate(12, -34)));

        assertFalse(inLowerHalfPlane(new Coordinate(0, 1)));
        assertFalse(inLowerHalfPlane(new Coordinate(1, 1)));
        assertFalse(inLowerHalfPlane(new Coordinate(-1, 1)));
        assertFalse(inLowerHalfPlane(new Coordinate(12, 34)));
    }

    /**
     * @param coordinate we want to test, whether it lies in the upper right quadrant
     * @return {@code true} if {@code coordinate} lies in the upper right quadrant.
     * {@code false} otherwise.
     */
    private boolean inUpperRightQuadrant(Coordinate coordinate) {
        return JTSUtils.pointInAngle(
                new Coordinate(1, 0),
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                coordinate
        );
    }

    /**
     * @param coordinate we want to test, whether it lies in the upper half plane
     * @return {@code true} if {@code coordinate} lies in the upper half plane.
     * {@code false} otherwise.
     */
    private boolean inUpperHalfPlane(Coordinate coordinate) {
        return JTSUtils.pointInAngle(
                new Coordinate(1, 0),
                new Coordinate(0, 0),
                new Coordinate(-1, 0),
                coordinate
        );
    }

    /**
     * @param coordinate we want to test, whether it lies in the lower half plane
     * @return {@code true} if {@code coordinate} lies in the lower half plane.
     * {@code false} otherwise.
     */
    private boolean inLowerHalfPlane(Coordinate coordinate) {
        return JTSUtils.pointInAngle(
                new Coordinate(-1, 0),
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                coordinate
        );
    }
}
