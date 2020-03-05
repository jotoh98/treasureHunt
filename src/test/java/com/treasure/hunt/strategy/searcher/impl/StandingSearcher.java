package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.SearchPath;
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
    public SearchPath move() {
        return new SearchPath(startPosition);
    }

    @Override
    public SearchPath move(Hint hint) {
        return new SearchPath(startPosition);
    }
}
