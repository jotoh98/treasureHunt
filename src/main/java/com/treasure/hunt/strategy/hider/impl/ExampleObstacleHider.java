package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.obstacles.ObstacleHider;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import org.locationtech.jts.geom.Point;

import java.util.List;

/**
 * @author axel12
 */
public class ExampleObstacleHider implements ObstacleHider<CircleHint> {
    @Override
    public CircleHint move(Movement movement) {
        return null;
    }

    @Override
    public void init(List<GeometryItem> obstacles) {
    }

    @Override
    public Point getTreasureLocation() {
        return null;
    }
}
