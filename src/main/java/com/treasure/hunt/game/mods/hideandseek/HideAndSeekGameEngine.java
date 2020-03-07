package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.SearchPathPrototype;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;

/**
 * In this modification, the hider may reset the
 * treasure location in each move,
 * after the {@link Searcher} did his {@link SearchPathPrototype}.
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
        treasurePos = hider.getTreasureLocation().getCoordinate(); // Difference between GameEngine and HideAndSeekGameEngine.
        lastHint = hider.move(lastSearchPath);
        assert (lastHint != null);
        verifyHint(lastHint, treasurePos);
    }
}
