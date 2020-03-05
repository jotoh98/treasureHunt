package com.treasure.hunt.jts.geom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class HalfPlaneTest {

    @Test
    void insideTest() {
        HalfPlane upperHalfPlane = new HalfPlane(new Coordinate(1, 0), new Coordinate(0, 0));
        Coordinate upperCoordinate = new Coordinate(0, 1);
        Coordinate lowerCoordinate = new Coordinate(0, -1);

        Assertions.assertTrue(upperHalfPlane.inside(upperCoordinate));
        Assertions.assertFalse(upperHalfPlane.inside(lowerCoordinate));
    }

}