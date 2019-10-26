package com.treasure.hunt.game.mods.insecurehints;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.tipster.Tipster;

public interface InsecureTipster extends Tipster {
    /**
     * Use this to initialize your Tipster.
     *
     * @param insecurity  the probability, the given hint is correct. It must fulfill 0<=insecurity<=1
     * @param gameHistory the gameHistory, the {@link Tipster} dumps its thoughts.
     */
    void init(double insecurity, GameHistory gameHistory);
}
