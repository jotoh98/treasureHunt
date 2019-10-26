package com.treasure.hunt.game.mods.obstacles;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.tipster.Tipster;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface ObstacleTipster extends Tipster {

    /**
     * Use this to initialize your Tipster.
     * This will tell him the treasure position,
     * the obstacles and
     * give him the GameHistory.
     *
     * @param treasurePosition The initial treasure position
     * @param obstacles the obstacles, placed in the plane
     * @param gameHistory The gameHistory, the tipster dumps its geometry objects by Tipster.commitProduct
     */
    void init(Point treasurePosition, List<GeometryItem> obstacles, GameHistory gameHistory);

}
