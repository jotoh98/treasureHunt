package com.treasure.hunt.game.mods.insecurehints;

import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

/**
 * Nothing to implement here, BUT
 * you should ensure, your {@link Searcher} can handle insecure hints.
 *
 * @param <T> the type of {@link Hint} this {@link Searcher} can handle.
 * @author dorianreineccius
 */
public interface InsecureSearcher<T extends Hint> extends Searcher<T> {

    /**
     * Use this to initialize/reset your searcher.
     *
     * @param startPosition the position, the searcher starts on
     * @param insecurity    the probability, the {@link com.treasure.hunt.strategy.hint.Hint} of the {@link InsecureHider} is correct.
     */
    // TODO not sure, whether the searcher may know the insecurity
    void reset(Point startPosition, double insecurity);
}
