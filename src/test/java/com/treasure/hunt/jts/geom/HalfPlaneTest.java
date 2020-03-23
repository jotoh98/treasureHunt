package com.treasure.hunt.jts.geom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.stream.IntStream;

class HalfPlaneTest {

    @Test
    void test() {
        HalfPlane halfPlane = new HalfPlane(new Coordinate(0, 0), new Coordinate(1, 0));
        IntStream.range(0, 50)
                .mapToObj(value -> new Coordinate(-10 + Math.random() * 20, Math.random() * 20))
                .forEach(coordinate ->
                        Assertions.assertFalse(halfPlane.inside(coordinate))
                );
        IntStream.range(0, 50)
                .mapToObj(value -> new Coordinate(-10 + Math.random() * 20, -Math.random() * 20))
                .forEach(coordinate ->
                        Assertions.assertTrue(halfPlane.inside(coordinate))
                );
    }

}