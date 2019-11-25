package com.treasure.hunt.strategy.searcher;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import org.locationtech.jts.geom.Point;

public interface Searcher<T extends Hint> {

    /**
     * Use this to initialize your searcher.
     *
     * @param startPosition the searchers starting position
     */
    void init(Point startPosition);

    /**
     * Use this to perform a initial move, without a hint given.
     * This is for the case, the searcher starts. (as he does normally)
     *
     * @return Moves the {@link Movement} the searcher did
     */
    Movement move();

    /**
     * @param hint the hint, the {@link Hider} gave last.
     * @return Moves the {@link Movement}, the searcher chose.
     */
    Movement move(T hint);
}
