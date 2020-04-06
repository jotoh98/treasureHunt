package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;
import org.locationtech.jts.geom.Point;

/**
 * In this modification, the hider may reset the
 * treasure location in each move,
 * after the {@link Searcher} did his {@link SearchPath}.
 *
 * @author dorianreineccius
 */
@Requires(hider = HideAndSeekHider.class, searcher = Searcher.class)
public class HideAndSeekGameEngine extends GameEngine {
    public HideAndSeekGameEngine(Searcher searcher, Hider hider) {
        super(searcher, hider);
    }

    public HideAndSeekGameEngine(Searcher searcher, Hider hider, Point point) {
        super(searcher, hider, point);
    }

    /**
     * Let the {@link GameEngine#hider} reset the treasure position and give his {@link com.treasure.hunt.strategy.hint.Hint}.
     */
    @Override
    protected void hiderMove() {
        Hint newHint = hider.move(lastSearchPath);
        treasurePos = hider.getTreasureLocation(); // Difference between GameEngine and HideAndSeekGameEngine.
        verifyHint(lastHint, newHint, treasurePos, lastSearchPath.getLastPoint());
        lastHint = newHint;
    }

}
