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
     * @param width                 the width of the playing area
     * @param height                the height of the playing area
     */
    void init(Point searcherStartPosition, int width, int height);

    /**
     * Use this to perform a initial move, without a hint given.
     * This is for the case, the searcher starts. (as he does normally)
     *
     * @return {@link Movement} the {@link Movement} the searcher did
     */
    Movement move();

    /**
     * @param hint the hint, the {@link Hider} gave last.
     * @return {@link Movement} the {@link Movement}, this searcher chose.
     */
    Movement move(T hint);
}
