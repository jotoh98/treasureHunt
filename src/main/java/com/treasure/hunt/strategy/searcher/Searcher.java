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

    /**
     * @param searcherStartPosition the {@link com.treasure.hunt.strategy.searcher.Searcher} starting position,
     *                              he will initialized on.
     */
    void init(Point searcherStartPosition);

    /**
     * Use this to perform a initial move, without a hint given.
     * This is for the case, the searcher starts. (as he does normally)
     *
     * @return {@link SearchPath} the {@link SearchPath} the searcher did
     */
    SearchPath move();

    /**
     * @param hint the hint, the {@link Hider} gave last.
     * @return {@link SearchPath} the {@link SearchPath}, this searcher chose.
     */
    SearchPath move(T hint);
}
