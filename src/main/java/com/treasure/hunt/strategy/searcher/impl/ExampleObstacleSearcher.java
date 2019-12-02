package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.game.mods.obstacles.ObstacleSearcher;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import org.locationtech.jts.geom.Point;

import java.util.List;

/**
 * @author axel12
 */
public class ExampleObstacleSearcher implements ObstacleSearcher<CircleHint> {
    @Override
    public void init(Point startPosition, List<GeometryItem> obstacles) {

    }

    @Override
    public void init(Point startPosition) {

    }

    @Override
    public Movement move() {
        return null;
    }

    @Override
    public Movement move(CircleHint hint) {
        return null;
    }
}
