package com.treasure.hunt.strategy.hider;

import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

public interface Hider<T extends Hint> {

    /**
     * This should output the current treasure location,
     * the {@link Hider} is able to change.
     *
     * @return Point the new treasure location
     */
    Point getTreasureLocation();

    /**
     * Use this to tell a hint,
     * knowing the last Moves of the {@link Searcher}.
     *
     * @param movement the moves, the {@link Searcher} did last.
     * @return T a hint.
     */
    T move(Movement movement);
}