package com.treasure.hunt.game.mods.insecurehints;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;
import com.treasure.hunt.view.in_game.View;

import java.util.List;

@Requires(hider = InsecureHider.class, searcher = InsecureSearcher.class)
public class InsecureGameManager extends GameManager {

    public InsecureGameManager(Searcher searcher, Hider hider, List<View> view) {
        super(searcher, hider, view);
    }
}
