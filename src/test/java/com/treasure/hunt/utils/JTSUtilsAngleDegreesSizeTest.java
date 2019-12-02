package com.treasure.hunt.utils;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testbench, testing {@link JTSUtils#angleDegreesSize(Coordinate, Coordinate, Coordinate, double)}.
 *
 * @author dorianreineccius
 */
class JTSUtilsAngleDegreesSizeTest {
    @Test
    void pointInAngle1() {
        assertTrue(JTSUtils.angleDegreesSize(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1), Math.PI / 2 + 0.00001));
        assertFalse(JTSUtils.angleDegreesSize(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(-1, 1), Math.PI / 2));
        assertFalse(JTSUtils.angleDegreesSize(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(-1, -0.0001), Math.PI));
    }
}
