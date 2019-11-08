package com.treasure.hunt.strategy.searcher.movesImplementations;

import com.treasure.hunt.strategy.searcher.Moves;
import org.locationtech.jts.geom.Point;

public class Steps extends Moves {
    public void addStep(Point step){
        addWayPoint(step);
    }
}
