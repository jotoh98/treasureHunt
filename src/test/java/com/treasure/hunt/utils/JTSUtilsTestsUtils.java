package com.treasure.hunt.utils;

import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An utility class for testing whether Points are approximately equal,
 * with an error tolerance of {@link JTSUtilsTestsUtils#ROUNDING_ERROR}.
 *
 * @author dorianreineccius
 */
class JTSUtilsTestsUtils {
    public final static double ROUNDING_ERROR = 0.00001;

    /**
     * Tests, whether two {@link Coordinate}'s are approximately equal.
     *
     * @param expected {@link Coordinate}
     * @param actual   {@link Coordinate}
     */
    static void eq(Coordinate expected, Coordinate actual) {
        assertTrue(Math.abs(expected.x - actual.x) < ROUNDING_ERROR &&
                        Math.abs(expected.y - actual.y) < ROUNDING_ERROR,
                "Expected: x≈" + expected.x + " and y≈" + expected.y + ", but was ≈(" + actual.x + ", " + actual.y + ").");
    }

    /**
     * Tests, whether two {@link Coordinate}'s are approximately unequal.
     *
     * @param expected {@link Coordinate}
     * @param actual   {@link Coordinate}
     */
    static void neq(Coordinate expected, Coordinate actual) {
        assertTrue(Math.abs(expected.x - actual.x) > ROUNDING_ERROR ||
                        Math.abs(expected.y - actual.y) > ROUNDING_ERROR,
                "Expected: x≠" + expected.x + " or y≠" + expected.y + ", but was ≈(" + actual.x + ", " + actual.y + ").");
    }
}
