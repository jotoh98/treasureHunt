package com.treasure.hunt.strategy.searcher.impl.ias;

import org.locationtech.jts.geom.Polygon;

/*
    Interface to implement a movement-strategy for the IntelligentAngleSearcher.

 * @author Vincent Schönbach
 */

public interface IASMovementStrategy {

    void run(IntelligentAngleSearcher searcher);
}
