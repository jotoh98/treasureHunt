package com.treasure.hunt.strategy.hider.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import org.locationtech.jts.geom.Point;

public class ExampleHider implements Hider<AngleHint> {
    @Override
    public Point getTreasureLocation() {
        return null;
    }

    @Override
    public void init(GameHistory gameHistory) {

    }

    @Override
    public AngleHint move(Moves moves) {
        return null;
    }
}
