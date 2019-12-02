package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import org.locationtech.jts.geom.Point;

/**
 * @author axel12
 */
public class ExampleHider implements Hider<AngleHint> {
    @Override
    public Point getTreasureLocation() {
        return null;
    }

    @Override
    public AngleHint move(Movement movement) {
        return null;
    }
}
