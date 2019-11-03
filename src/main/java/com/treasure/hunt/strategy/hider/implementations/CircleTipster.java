package com.treasure.hunt.strategy.tipster.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hint.CircleHint;
import com.treasure.hunt.strategy.seeker.Moves;
import com.treasure.hunt.strategy.tipster.Tipster;
import org.locationtech.jts.geom.Point;

public class CircleTipster implements Tipster<CircleHint> {
    @Override
    public void init(Point treasurePosition, GameHistory gameHistory) {

    }

    @Override
    public void commitProduct(Product product) {

    }

    @Override
    public CircleHint move(Moves moves) {
        return null;
    }
}
