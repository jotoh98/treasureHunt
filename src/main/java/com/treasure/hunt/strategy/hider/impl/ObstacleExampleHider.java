package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.obstacles.ObstacleHider;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import org.locationtech.jts.geom.Point;

import java.util.List;

public class ObstacleExampleHider implements ObstacleHider<CircleHint> {
    @Override
    public void init(Point treasurePosition, List<GeometryItem> obstacles) {
    }

    @Override
    public Point getTreasureLocation() {
        return null;
    }

    @Override
    public CircleHint move(Movement movement) {
        return null;
    }
}
