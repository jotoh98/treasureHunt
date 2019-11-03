package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.view.in_game.View;

import java.util.List;

public class HideAndSeekGameManager extends GameManager {

    protected HideAndSeekHider hider;
    protected HideAndSeekSearcher searcher;

    public HideAndSeekGameManager(HideAndSeekSearcher searcher, HideAndSeekHider hider, List<View> view) {
        super(searcher, hider, view);
    }

    /**
     * This simulates just one step of the simulation.
     * The searcher begins since we want not force him,
     * to take a initial hint, he eventually do not need,
     * if he works randomized!
     * <p>
     * The first step of the searcher goes without an hint,
     * the next will be with.
     * <p>
     * After each move of the {@link HideAndSeekHider}, the treasure position
     * will be updated, but it could have not changed.
     */
    public void step() {
        super.step();
        treasurePos = hider.getTreasureLocation();
    }
}
