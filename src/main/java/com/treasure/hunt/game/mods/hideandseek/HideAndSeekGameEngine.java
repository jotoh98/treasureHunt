package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;
import org.locationtech.jts.geom.Coordinate;

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

    public HideAndSeekGameEngine(Searcher searcher, Hider hider, Coordinate coordinate) {
        super(searcher, hider, coordinate);
    }

    /**
     * Let the {@link GameEngine#hider} reset the treasure position and give his {@link com.treasure.hunt.strategy.hint.Hint}.
     */
    protected void moveHider() {
        treasurePos = hider.getTreasureLocation(); // Difference between GameEngine and HideAndSeekGameEngine.
        lastHint = hider.move(lastSearchPath);
        assert (lastHint != null);
        verifyHint(lastHint, treasurePos, lastSearchPath.getLastPoint());
    }

    /**
     * Let the {@link GameEngine#hider} reset the treasure position and give his {@link com.treasure.hunt.strategy.hint.Hint}.
     */
    protected void hiderMove() {
        lastHint = hider.move(lastSearchPath);
        treasurePos = hider.getTreasureLocation(); // Difference between GameEngine and HideAndSeekGameEngine.
        assert (lastHint != null);
        verifyHint(lastHint, treasurePos);
    }
}
