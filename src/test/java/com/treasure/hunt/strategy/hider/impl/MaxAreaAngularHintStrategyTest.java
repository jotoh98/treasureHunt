package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.GameHistory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxAreaAngularHintStrategyTest {
    private MaxAreaAngularHintStrategy strat;
    private GeometryFactory gf;

    @BeforeAll
    void init() {
        gf = new GeometryFactory(); //TODO swap out with externally defined default factory for project

        strat = new MaxAreaAngularHintStrategy();
        strat.init(gf.createPoint(new Coordinate(10.0,10.0)), new GameHistory());
    }

    @Test
    void isClockwiseOrdered() {
        Coordinate[] coords = new Coordinate[]{new Coordinate(0.0,0.0), new Coordinate(2.0,0.0), new Coordinate(2.0,2.0) };
        assertFalse(strat.isClockwiseOrdered(coords));

        coords =  new Coordinate[]{new Coordinate(0.0,0.0), new Coordinate(2.0,2.0), new Coordinate(2.0,0.0) };
        assertTrue(strat.isClockwiseOrdered(coords));

    }
}