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
        assertTrue(inAngle1(new Coordinate(0, 0)));
        assertTrue(inAngle1(new Coordinate(0, 1)));
        assertTrue(inAngle1(new Coordinate(1, 0)));
        assertTrue(inAngle1(new Coordinate(1, 1)));
        assertTrue(inAngle1(new Coordinate(76, 23)));

        assertFalse(inAngle1(new Coordinate(-1, 0)));
        assertFalse(inAngle1(new Coordinate(0, -1)));
        assertFalse(inAngle1(new Coordinate(-1, -1)));
        assertFalse(inAngle1(new Coordinate(-54, -23)));
    }

    @Test
    void pointInAngle2() {
        assertTrue(inAngle2(new Coordinate(0, 0)));
        assertTrue(inAngle2(new Coordinate(0, 1)));
        assertTrue(inAngle2(new Coordinate(1, 0)));
        assertTrue(inAngle2(new Coordinate(1, 1)));
        assertTrue(inAngle2(new Coordinate(-1, 0)));
        assertTrue(inAngle2(new Coordinate(-1, 1)));
        assertTrue(inAngle2(new Coordinate(12, 34)));

        assertFalse(inAngle2(new Coordinate(0, -1)));
        assertFalse(inAngle2(new Coordinate(1, -1)));
        assertFalse(inAngle2(new Coordinate(-1, -1)));
        assertFalse(inAngle2(new Coordinate(12, -34)));
    }

    @Test
    void pointInAngle3() {
        assertTrue(inAngle3(new Coordinate(0, 0)));
        assertTrue(inAngle3(new Coordinate(0, -1)));
        assertTrue(inAngle3(new Coordinate(1, 0)));
        assertTrue(inAngle3(new Coordinate(1, -1)));
        assertTrue(inAngle3(new Coordinate(-1, 0)));
        assertTrue(inAngle3(new Coordinate(-1, -1)));
        assertTrue(inAngle3(new Coordinate(12, -34)));

        assertFalse(inAngle3(new Coordinate(0, 1)));
        assertFalse(inAngle3(new Coordinate(1, 1)));
        assertFalse(inAngle3(new Coordinate(-1, 1)));
        assertFalse(inAngle3(new Coordinate(12, 34)));
    }

    private boolean inAngle1(Coordinate coordinate) {
        return JTSUtils.pointInAngle(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(0, 1),
                coordinate
        );
    }

    private boolean inAngle2(Coordinate coordinate) {
        return JTSUtils.pointInAngle(
                new Coordinate(1, 0),
                new Coordinate(0, 0),
                new Coordinate(-1, 0),
                coordinate
        );
    }

    private boolean inAngle3(Coordinate coordinate) {
        return JTSUtils.pointInAngle(
                new Coordinate(-1, 0),
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                coordinate
        );
    }
}
