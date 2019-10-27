package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.view.in_game.View;

import java.util.List;

public class HideAndSeekGameManager extends GameManager {

    protected HideAndSeekSeeker seeker;
    protected HideAndSeekTipster tipster;

    public HideAndSeekGameManager(HideAndSeekSeeker seeker, HideAndSeekTipster tipster, List<View> view) {
        super(seeker, tipster, view);
    }

    /**
     * This simulates just one step of the simulation.
     * The seeker begins since we want not force him,
     * to take a initial hint, he eventually do not need,
     * if he works randomized!
     * <p>
     * The first step of the seeker goes without an hint,
     * the next will be with.
     * <p>
     * After each move of the {@link HideAndSeekTipster}, the treasure position
     * will be updated, but it could have not changed.
     */
    public void step() {
        super.step();
        treasurePos = tipster.getTreasureLocation();
    }
}
