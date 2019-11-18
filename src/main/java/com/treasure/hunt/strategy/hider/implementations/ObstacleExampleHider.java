package com.treasure.hunt.strategy.hider.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.game.mods.obstacles.ObstacleHider;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.CircleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import org.locationtech.jts.geom.Point;

import java.util.List;

public class ObstacleExampleHider implements ObstacleHider<CircleHint> {
    @Override
    public void init(Point treasurePosition, List<GeometryItem> obstacles, GameHistory gameHistory) {

    }

    @Override
    public Point getTreasureLocation() {
        return null;
    }

    @Override
    public void init(GameHistory gameHistory) {

    }

    @Override
    public CircleHint move(Moves moves) {
        return null;
    }
}
