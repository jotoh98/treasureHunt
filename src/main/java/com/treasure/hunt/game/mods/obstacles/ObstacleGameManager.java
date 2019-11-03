package com.treasure.hunt.game.mods.obstacles;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.utils.Requires;
import com.treasure.hunt.view.in_game.View;

import java.util.List;

@Requires(hider = ObstacleHider.class, searcher = ObstacleSearcher.class)
public class ObstacleGameManager extends GameManager {

    protected ObstacleHider hider;
    protected ObstacleSearcher searcher;
    protected List<GeometryItem> obstacles;

    public ObstacleGameManager(ObstacleSearcher searcher, ObstacleHider hider, List<View> view, List<GeometryItem> obstacles) {
        super(searcher, hider, view);
    }

    @Override
    protected boolean checkConsistency() {
        // TODO, check whether the searcher passed a wall!
        return true;
    }
}
