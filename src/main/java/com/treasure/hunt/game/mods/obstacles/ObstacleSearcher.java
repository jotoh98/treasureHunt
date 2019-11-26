package com.treasure.hunt.game.mods.obstacles;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface ObstacleSearcher<T extends Hint> extends Searcher<T> {

    /**
     * Use this to initialize your searcher.
     *
     * @param startPosition the position, the searcher starts
     * @param obstacles     the obstacles placed in the game
     */
    void init(Point startPosition, List<GeometryItem> obstacles);

}
