package com.treasure.hunt.strategy.hider;

import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

/**
 * An algorithm,
 * hiding a treasure and giving a searcher {@link Hint}-objects,
 * such he can find the treasure.
 *
 * @param <T> The type of {@link Hint}, this hider can handle.
 * @author dorianreineccius
 */
public interface Hider<T extends Hint> {
    /**
     * @param searcherStartPosition the {@link com.treasure.hunt.strategy.searcher.Searcher} starting position,
     *                              he will initialized on.
     * @param width                 the width of the playing area
     * @param height                the height of the playing area
     */
    void init(Point searcherStartPosition, int width, int height);

    /**
     * @param movement the {@link Movement}, the {@link Searcher} did last
     * @return T a (new) hint.
     */
    T move(Movement movement);

    /**
     * @return the current treasure location
     */
    Point getTreasureLocation();
}