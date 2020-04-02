package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

/**
 * A test-{@link Hider}, placing the treasure on the {@link com.treasure.hunt.strategy.searcher.Searcher}'s
 * spawn position (0,0).
 *
 * @author dorianreineccius
 */
public class InstantWinHider implements Hider<Hint> {

    /**
     * The Hider spawns the treasure on the searchers starting point.
     *
     * @return The {@link Point} with treasure location
     */
    @Override
    public Point getTreasureLocation() {
        return JTSUtils.createPoint(0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Point searcherStartPosition) {
    }

    /**
     * This should not be called, since this type of {@link Hider} places the treasure on the {@link com.treasure.hunt.strategy.searcher.Searcher}'s
     * spawn point, such that the game is finished instantly.
     *
     * @param searchPath the {@link SearchPath}, the {@link com.treasure.hunt.strategy.searcher.Searcher} did last
     * @return {@link IllegalStateException}, since this is not be called.
     */
    @Override
    public Hint move(SearchPath searchPath) {
        throw new IllegalStateException("This may not be called since the Searcher has already won.");
    }
}
