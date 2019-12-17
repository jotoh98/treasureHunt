package com.treasure.hunt.strategy.hider.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxAreaAngularHintStrategyTest {
    private MaxAreaAngularHintStrategy maxAreaAngularHintStrategy;

    @BeforeEach
    void init() {
        maxAreaAngularHintStrategy = new MaxAreaAngularHintStrategy();
    }

    @Test
    void isClockwiseOrdered() {
        Coordinate[] coords = new Coordinate[]{new Coordinate(0.0,0.0), new Coordinate(2.0,0.0), new Coordinate(2.0,2.0) };
        //assertFalse(maxAreaAngularHintStrategy.isClockwiseOrdered(coords));

        coords = new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(2.0, 2.0), new Coordinate(2.0, 0.0)};
        assertTrue(maxAreaAngularHintStrategy.isClockwiseOrdered(coords));

    }
}