package com.treasure.hunt.strategy.searcher.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.game.mods.obstacles.ObstacleSearcher;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.CircleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import org.locationtech.jts.geom.Point;

import java.util.List;

public class ExampleObstacleSearcher implements ObstacleSearcher<CircleHint> {
    @Override
    public void init(Point startPosition, List<GeometryItem> obstacles, GameHistory gameHistory) {

    }

    @Override
    public void init(Point startPosition, GameHistory gameHistory) {

    }

    @Override
    public Moves move() {
        return null;
    }

    @Override
    public Moves move(CircleHint hint) {
        return null;
    }

    @Override
    public Point getLocation() {
        return null;
    }
}
