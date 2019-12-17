package com.treasure.hunt.game.mods.obstacles;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;
import org.locationtech.jts.geom.Point;

/**
 * In this game modification,
 * obstacles are placed in the map.
 *
 * @author dorianreineccius
 */
@Requires(hider = ObstacleHider.class, searcher = ObstacleSearcher.class)
public class ObstacleGameEngine extends GameEngine {
    public ObstacleGameEngine(Searcher searcher, Hider hider) {
        super(searcher, hider);
    }

    /**
     * Checks, whether the {@link Searcher} followed the rules and whether the game is consistent.
     * TODO Check, whether the searcher passed a wall.
     */
    @Override
    protected void verifyMovement(Movement movement, Point initialSearcherPosition) {
    }
}
