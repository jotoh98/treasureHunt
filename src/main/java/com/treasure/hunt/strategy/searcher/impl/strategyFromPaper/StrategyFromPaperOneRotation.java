package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

public class StrategyFromPaperOneRotation implements Searcher<HalfPlaneHint> {

    private StrategyFromPaper strategy;

    /**
     * @param searcherStartPosition the {@link Searcher} starting position,
     *                              he will initialized on.
     */
    @Override
    public void init(Point searcherStartPosition) {
        strategy = new StrategyFromPaper(1);
        strategy.init(searcherStartPosition);
    }

    /**
     * Use this to perform a initial move, without a hint given.
     * This is for the case, the searcher starts. (as he does normally)
     *
     * @return {@link Movement} the {@link Movement} the searcher did
     */
    @Override
    public Movement move() {
        return strategy.move();
    }

    /**
     * @param hint the hint, the {@link Hider} gave last.
     * @return {@link Movement} the {@link Movement}, this searcher chose.
     */
    @Override
    public Movement move(HalfPlaneHint hint) {
        return strategy.move(hint);
    }
}
