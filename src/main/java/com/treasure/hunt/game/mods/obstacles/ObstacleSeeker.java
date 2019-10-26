package com.treasure.hunt.game.mods.obstacles;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.seeker.Seeker;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface ObstacleSeeker extends Seeker {

    /**
     * Use this to initialize your Seeker.
     * This will tell him the starting position
     * tell him the obstacles placed in the plane and
     * give him the GameHistory.
     *
     * @param startPosition the position, the seeker starts
     * @param obstacles the obstacles placed in the plane
     * @param gameHistory the gameHistory to dump in the list of {@link GeometryItem}
     */
    void init(Point startPosition, List<GeometryItem> obstacles, GameHistory gameHistory);

}
