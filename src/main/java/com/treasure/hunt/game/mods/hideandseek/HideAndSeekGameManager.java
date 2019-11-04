package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;
import com.treasure.hunt.view.in_game.View;

import java.util.List;


@Requires(hider = HideAndSeekHider.class, searcher = HideAndSeekSearcher.class)
public class HideAndSeekGameManager extends GameManager {

    protected final HideAndSeekHider hideAndSeekHider;

    public HideAndSeekGameManager(Searcher searcher, Hider hider, List<View> view) {
        super(searcher, hider, view);
        hideAndSeekHider = (HideAndSeekHider) hider;
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
        treasurePos = hideAndSeekHider.getTreasureLocation();
    }
}
