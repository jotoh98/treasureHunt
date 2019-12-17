package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

/**
 * This test {@link Searcher} just keeps standing on his starting position.
 *
 * @author dorianreineccius
 */
public class StandingSearcher implements Searcher<Hint> {
    private Point startPosition;

    @Override
    public void init(Point startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public Movement move() {
        return new Movement(startPosition);
    }

    @Override
    public Movement move(Hint hint) {
        return new Movement(startPosition);
    }
}
