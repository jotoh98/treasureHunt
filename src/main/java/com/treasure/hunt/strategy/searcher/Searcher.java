package com.treasure.hunt.strategy.searcher;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import org.locationtech.jts.geom.Point;

/**
 * An algorithm, trying to find a treasure in the plain,
 * given {@link Hint}-objects by a {@link Hider}.
 *
 * @param <T> the type of {@link Hint}, this searcher can handle.
 * @author dorianreineccius
 */
public interface Searcher<T extends Hint> {

    double SCANNING_DISTANCE = 1.0;

    /**
     * @param searcherStartPosition the {@link com.treasure.hunt.strategy.searcher.Searcher} starting position,
     *                              he will initialized on.
     */
    void init(Point searcherStartPosition);

    /**
     * Use this to perform a initial move, without a hint given.
     * This is for the case, the searcher starts. (as he does normally)
     *
     * @return {@link SearchPathPrototype} the {@link SearchPathPrototype} the searcher did
     */
    SearchPathPrototype move();

    /**
     * @param hint the hint, the {@link Hider} gave last.
     * @return {@link SearchPathPrototype} the {@link SearchPathPrototype}, this searcher chose.
     */
    SearchPathPrototype move(T hint);
}
