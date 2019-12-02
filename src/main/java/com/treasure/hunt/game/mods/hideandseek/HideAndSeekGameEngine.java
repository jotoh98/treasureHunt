package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.Move;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;

/**
 * In this modification, the hider may reset the
 * treasure location in each move.
 */
@Requires(hider = HideAndSeekHider.class, searcher = HideAndSeekSearcher.class)
public class HideAndSeekGameEngine extends GameEngine {

    public HideAndSeekGameEngine(Searcher searcher, Hider hider) {
        super(searcher, hider);
    }

    /**
     * This simulates just one step of the simulation.
     * The searcher begins since we want not force him,
     * to take a initial hint, he eventually do not need,
     * if he works randomized!
     * <p>
     * The first step of the searcher goes without an hint,
     * the next will be with.
     * <p>
     * After each move of the {@link HideAndSeekHider}, the treasure position
     * will be updated, but it could have not changed.
     */
    public Move move() {
        treasurePos = hider.getTreasureLocation();
        return super.move();
    }
}
