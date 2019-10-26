package com.treasure.hunt.game.mods.obstacles;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.view.in_game.View;

import java.util.List;

public class ObstacleGameManager extends GameManager {

    protected List<GeometryItem> obstacles;
    protected ObstacleSeeker seeker;
    protected ObstacleTipster tipster;

    public ObstacleGameManager(ObstacleSeeker seeker, ObstacleTipster tipster, List<View> view, List<GeometryItem> obstacles) {
        super(seeker, tipster, view);
    }

    @Override
    protected boolean checkConsistency() {
        // TODO, check whether the seeker passed a wall!
        return true;
    }
}
