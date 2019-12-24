package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import org.locationtech.jts.geom.Point;

public class CirlceHider implements Hider<CircleHint> {
    @Override
    public void init(Point searcherStartPosition) {

    }

    @Override
    public CircleHint move(SearchPath searchPath) {
        return null;
    }

    @Override
    public Point getTreasureLocation() {
        return null;
    }
}
