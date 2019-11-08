package com.treasure.hunt.strategy.hider.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.CircleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import org.locationtech.jts.geom.Point;

public class CircleHider implements Hider<CircleHint> {
    @Override
    public void commitProduct(Product product) {

    }

    @Override
    public Point getTreasureLocation() {
        return null;
    }

    @Override
    public void init(GameHistory gameHistory) {

    }

    @Override
    public CircleHint move(Moves moves) {
        return null;
    }
}
