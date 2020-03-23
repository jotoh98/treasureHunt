package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;

/**
 * In this modification, the hider may reset the
 * treasure location in each move,
 * after the {@link Searcher} did his {@link SearchPath}.
 *
 * @author dorianreineccius
 */
@Requires(hider = HideAndSeekHider.class, searcher = HideAndSeekSearcher.class)
public class HideAndSeekGameEngine extends GameEngine {
    public HideAndSeekGameEngine(Searcher searcher, Hider hider) {
        super(searcher, hider);
    }

    /**
     * Let the {@link GameEngine#hider} reset the treasure position and give his {@link com.treasure.hunt.strategy.hint.Hint}.
     */
    protected void moveHider() {
        treasurePos = hider.getTreasureLocation(); // Difference between GameEngine and HideAndSeekGameEngine.
        Hint newHint = hider.move(lastSearchPath);
        assert (newHint != null);
        verifyHint(newHint, treasurePos);
        lastHint = newHint;
    }
}
