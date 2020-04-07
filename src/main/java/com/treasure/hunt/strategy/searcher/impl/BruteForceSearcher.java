package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

/**
 * This type of {@link Searcher} runs in square-shaped circles around its
 * initial position, which increases by one, after each step.
 *
 * @author dorianreineccius
 */
public class BruteForceSearcher implements Searcher<Hint> {
    private static final int limit = 1;
    private int lineSegmentDistance = 0;
    private int x = 0, y = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Point startPosition) {
        this.x = (int) startPosition.getX();
        this.y = (int) startPosition.getY();
    }

    /**
     * A square-shaped circles around its
     * initial position, increased by one relative to the previous "circle".
     *
     * @author dorianreineccius
     */
    @Override
    public SearchPath move() {
        SearchPath searchPath = new SearchPath();
        for (int i = 0; i < limit; i++) {
            y += ++lineSegmentDistance;
            searchPath.addPoint(JTSUtils.createPoint(x, y));
            x += lineSegmentDistance;
            searchPath.addPoint(JTSUtils.createPoint(x, y));
            y -= ++lineSegmentDistance;
            searchPath.addPoint(JTSUtils.createPoint(x, y));
            x -= lineSegmentDistance;
            searchPath.addPoint(JTSUtils.createPoint(x, y));
        }
        return searchPath;
    }

    /**
     * @param hint the hint, the {@link com.treasure.hunt.strategy.hider.Hider} gave last.
     * @return {@link BruteForceSearcher#move(Hint)}.
     */
    @Override
    public SearchPath move(Hint hint) {
        return move();
    }
}
