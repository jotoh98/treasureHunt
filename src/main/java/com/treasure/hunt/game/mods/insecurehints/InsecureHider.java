package com.treasure.hunt.game.mods.insecurehints;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;

/**
 * @param <T> the type of {@link Hint} this {@link Hider} can handle.
 * @author dorianreineccius
 */
public interface InsecureHider<T extends Hint> extends Hider<T> {
    /**
     * Use this to initialize your hider.
     *
     * @param insecurity the probability, the given hint is correct.
     *                   It must fulfill 0 &le; insecurity &le; 1
     */
    void init(double insecurity);
}
