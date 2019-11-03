package com.treasure.hunt.strategy.hider.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import org.locationtech.jts.geom.Point;

public class RandomAngularHintStrategy implements Hider<AngleHint> {

    @Override
    public void init(Point treasurePosition, GameHistory gameHistory) {

    }

    @Override
    public void commitProduct(Product product) {

    }

    @Override
    public AngleHint move(Moves moves) {
        return null;
    }
}
