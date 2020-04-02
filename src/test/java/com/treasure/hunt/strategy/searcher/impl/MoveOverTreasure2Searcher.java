package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

/**
 * This test {@link Searcher} walks over the treasure,
 * but does not stop on it.
 *
 * @author dorianreineccius
 */
public class MoveOverTreasure2Searcher implements Searcher<CircleHint> {
    private Point startPosition;

    @Override
    public void init(Point startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public SearchPath move() {
        return new SearchPath(startPosition);
    }

    @Override
    public SearchPath move(CircleHint hint) {
        SearchPath searchPath = new SearchPath(startPosition);
        searchPath.addPoint(JTSUtils.createPoint(hint.getCircle().getCenter()));
        return searchPath;
    }
}
