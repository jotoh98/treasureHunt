package com.treasure.hunt.game.mods.obstacles;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface ObstacleHider<T extends Hint> extends Hider<T> {

    /**
     * Use this to initialize your hider.
     *
     * @param treasurePosition The initial treasure position
     * @param obstacles        the obstacles, placed in the game
     */
    void init(Point treasurePosition, List<GeometryItem> obstacles);

}
