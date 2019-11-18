package com.treasure.hunt.strategy.searcher.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.hint.CircleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

public class CircleSearcher implements Searcher<CircleHint> {

    @Override
    public void init(Point startPosition, GameHistory gameHistory) {

    }

    @Override
    public Moves move() {
        return null;
    }

    @Override
    public Moves move(CircleHint hint) {
        return null;
    }

    @Override
    public Point getLocation() {
        return null;
    }
}
