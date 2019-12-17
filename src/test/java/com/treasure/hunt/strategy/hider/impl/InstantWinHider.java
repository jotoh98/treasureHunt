package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

/**
 * A test-hider, placing the treasure on the {@link com.treasure.hunt.strategy.searcher.Searcher}'s
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
     * Nothing to do here
     */
    @Override
    public void reset() {
    }

    @Override
    public Hint move(Movement movement) {
        throw new IllegalStateException("This may not be called since the Searcher has already won.");
        //return null;
    }
}
