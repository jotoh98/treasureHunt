package com.treasure.hunt.game.mods.obstacles;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;
import com.treasure.hunt.view.in_game.View;

import java.util.List;

@Requires(hider = ObstacleHider.class, searcher = ObstacleSearcher.class)
public class ObstacleGameManager extends GameManager {

    public ObstacleGameManager(Searcher searcher, Hider hider, List<View> view) {
        super(searcher, hider, view);
    }

    @Override
    protected boolean checkConsistency() {
        // TODO, check whether the searcher passed a wall!
        return true;
    }
}
