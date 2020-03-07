package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.SearchPathPrototype;
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
    public void init(Point searcherStartPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public SearchPathPrototype move() {
        return new SearchPathPrototype(startPosition);
    }

    @Override
    public SearchPathPrototype move(Hint hint) {
        return new SearchPathPrototype(startPosition);
    }
}
