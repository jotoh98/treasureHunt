package com.treasure.hunt.game.mods.obstacles;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;

/**
 * In this game modification,
 * obstacles are placed in the map.
 */
@Requires(hider = ObstacleHider.class, searcher = ObstacleSearcher.class)
public class ObstacleGameEngine extends GameEngine {

    public ObstacleGameEngine(Searcher searcher, Hider hider) {
        super(searcher, hider);
    }

    @Override
    protected boolean checkConsistency() {
        // TODO, check whether the searcher passed a wall!
        return true;
    }
}
