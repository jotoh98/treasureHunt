package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

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

    @Override
    public Hint move(Movement movement) {
        throw new IllegalStateException("This may not be called since the Searcher has already won.");
        //return null;
    }
}
