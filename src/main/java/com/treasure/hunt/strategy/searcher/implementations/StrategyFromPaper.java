package com.treasure.hunt.strategy.searcher.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

public class StrategyFromPaper implements Searcher<AngleHint> {

    @Override
    public void init(Point startPosition, GameHistory gameHistory) {

    }

    @Override
    public void commitProduct(Product product) {

    }

    @Override
    public Moves move() {
        return null;
    }

    @Override
    public Moves move(AngleHint hint) {
        return null;
    }

    @Override
    public Point getLocation() {
        return null;
    }
}
