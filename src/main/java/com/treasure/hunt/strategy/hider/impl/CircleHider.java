package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import org.locationtech.jts.geom.Point;

public class CircleHider implements Hider<CircleHint> {
    @Override
    public Point getTreasureLocation() {
        return null;
    }

    @Override
    public CircleHint move(Movement movement) {
        return null;
    }
}
