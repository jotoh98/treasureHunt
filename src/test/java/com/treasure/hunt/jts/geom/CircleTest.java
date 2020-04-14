package com.treasure.hunt.jts.geom;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the class {@link Circle}.
 *
 * @author Dorian Reineccius
 */
public class CircleTest {
    /**
     * Tests for {@link Circle#contains(Circle)}.
     * The first {@link Circle} contains the given one.
     */
    @Test
    public void containsTest1() {
        assertTrue(new Circle(new Coordinate(0, 0), 2).contains(new Circle(new Coordinate(0, 0), 1)));
        assertTrue(new Circle(new Coordinate(0, 0), 1).contains(new Circle(new Coordinate(0, 0), 1)));
        assertTrue(new Circle(new Coordinate(0, 0), 4).contains(new Circle(new Coordinate(1, 0), 1)));
    }

    /**
     * Tests for {@link Circle#contains(Circle)}.
     * The first {@link Circle} is smaller than the given one.
     */
    @Test
    public void containsTest2() {
        assertFalse(new Circle(new Coordinate(0, 0), 1).contains(new Circle(new Coordinate(0, 0), 2)));
    }

    /**
     * Tests for {@link Circle#contains(Circle)}.
     * The first {@link Circle} and the given one are disjoint.
     */
    @Test
    public void containsTest3() {
        assertFalse(new Circle(new Coordinate(0, 0), 1).contains(new Circle(new Coordinate(2, 0), 1)));
    }

    /**
     * Tests for {@link Circle#contains(Circle)}.
     * The first {@link Circle} does not contain the given one completely.
     */
    @Test
    public void containsTest4() {
        assertFalse(new Circle(new Coordinate(0, 0), 2).contains(new Circle(new Coordinate(2, 0), 1)));
    }
}
