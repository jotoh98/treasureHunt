package com.treasure.hunt.game.mods.insecurehints;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.view.in_game.View;

import java.util.List;

public class InsecureGameManager extends GameManager {

    protected InsecureSearcher searcher;
    protected InsecureHider hider;

    public InsecureGameManager(InsecureSearcher searcher, InsecureHider hider, List<View> view) {
        super(searcher, hider, view);
    }
}
