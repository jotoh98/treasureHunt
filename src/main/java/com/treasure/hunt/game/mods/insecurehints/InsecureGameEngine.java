package com.treasure.hunt.game.mods.insecurehints;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;

/**
 * In this modification, the hints of the hider are wrong with
 * a probability of insecurity.
 */
@Requires(hider = InsecureHider.class, searcher = InsecureSearcher.class)
public class InsecureGameEngine extends GameEngine {

    public InsecureGameEngine(Searcher searcher, Hider hider) {
        super(searcher, hider);
    }
}
