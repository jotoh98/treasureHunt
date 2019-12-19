package com.treasure.hunt.utils;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testbench, testing {@link JTSUtils#pointInAngle(Coordinate, Coordinate, Coordinate, Coordinate)}.
 *
 * @author dorianreineccius
 */
class JTSUtilsPointInAngleTest {
    @Test
    void pointInAngle1() {
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

    @Test
    void pointInAngle2() {
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

    @Test
    void pointInAngle3() {
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

    private boolean inUpperRightQuadrant(Coordinate coordinate) {
        return JTSUtils.pointInAngle(
                new Coordinate(1, 0),
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                coordinate
        );
    }

    private boolean inUpperHalfPlane(Coordinate coordinate) {
        return JTSUtils.pointInAngle(
                new Coordinate(1, 0),
                new Coordinate(0, 0),
                new Coordinate(-1, 0),
                coordinate
        );
    }

    private boolean inLowerHalfPlane(Coordinate coordinate) {
        return JTSUtils.pointInAngle(
                new Coordinate(-1, 0),
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                coordinate
        );
    }
}
