package com.treasure.hunt.game.mods.insecurehints;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.hider.Hider;

public interface InsecureHider extends Hider {
    /**
     * Use this to initialize your hider.
     *
     * @param insecurity  the probability, the given hint is correct. It must fulfill 0<=insecurity<=1
     * @param gameHistory the gameHistory, the {@link Hider} dumps its thoughts.
     */
    void init(double insecurity, GameHistory gameHistory);
}
