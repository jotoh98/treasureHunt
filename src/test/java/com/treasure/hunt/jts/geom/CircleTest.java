package com.treasure.hunt.jts.geom;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CircleTest {

    /**
     * Tests for {@link Circle#contains(Circle)}
     */
    @Test
    public void containsTest() {
        assertTrue(new Circle(new Coordinate(0, 0), 2).contains(new Circle(new Coordinate(0, 0), 1)));
        assertTrue(new Circle(new Coordinate(0, 0), 1).contains(new Circle(new Coordinate(0, 0), 1)));
        assertTrue(new Circle(new Coordinate(0, 0), 4).contains(new Circle(new Coordinate(1, 0), 1)));
        // small circle cannot contain a bigger one
        assertFalse(new Circle(new Coordinate(0, 0), 1).contains(new Circle(new Coordinate(0, 0), 2)));
        // circles disjoint
        assertFalse(new Circle(new Coordinate(0, 0), 1).contains(new Circle(new Coordinate(2, 0), 1)));
        // circle not completely containing.
        assertFalse(new Circle(new Coordinate(0, 0), 2).contains(new Circle(new Coordinate(2, 0), 1)));
    }
}
