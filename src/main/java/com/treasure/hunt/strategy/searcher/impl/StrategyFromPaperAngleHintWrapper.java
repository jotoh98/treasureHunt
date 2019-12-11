package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

public class StrategyFromPaperAngleHintWrapper implements Searcher<AngleHint> {

    StrategyFromPaper strategyFromPaper;

    /**
     * @param startPosition the searchers starting position, he will initialized with.
     */
    @Override
    public void init(Point startPosition) {
        strategyFromPaper = new StrategyFromPaper();
        strategyFromPaper.init(startPosition);
    }

    /**
     * Use this to perform a initial move, without a hint given.
     * This is for the case, the searcher starts. (as he does normally)
     *
     * @return {@link Movement} the {@link Movement} the searcher did
     */
    @Override
    public Movement move() {
        return strategyFromPaper.move();
    }

    /**
     * @param hint the hint, the {@link Hider} gave last.
     * @return {@link Movement} the {@link Movement}, this searcher chose.
     */
    @Override
    public Movement move(AngleHint hint) {
        HalfPlaneHint halfPlaneHint = new HalfPlaneHint(hint.getCenter(), hint.getAnglePointRight());
        return strategyFromPaper.move(halfPlaneHint);
    }
}
