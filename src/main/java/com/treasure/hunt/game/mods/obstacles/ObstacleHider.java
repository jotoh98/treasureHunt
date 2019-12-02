package com.treasure.hunt.game.mods.obstacles;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;

import java.util.List;

/**
 * @param <T> the type of {@link Hint} this {@link Hider} can handle.
 * @author dorianreineccius
 */
public interface ObstacleHider<T extends Hint> extends Hider<T> {

    /**
     * Use this to initialize your hider.
     *
     * @param obstacles the obstacles, placed in the game
     */
    void init(List<GeometryItem> obstacles);

}
