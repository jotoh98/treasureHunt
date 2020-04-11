package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

/**
 * This test {@link Searcher} walks on the treasure and leaves it,
 * in the same {@link SearchPath}.
 *
 * @author Dorian Reineccius
 */
public class MoveOverTreasure1Searcher implements Searcher<CircleHint> {
    /**
     * This does nothing.
     */
    @Override
    public void init(Point startPosition) {
    }

    /**
     * @return An empty {@link SearchPath}
     */
    @Override
    public SearchPath move() {
        return new SearchPath();
    }

    /**
     * @param hint the hint, the {@link com.treasure.hunt.strategy.hider.Hider} gave last
     * @return A {@link SearchPath}, walking over the center of the last {@code hint}.
     * Especially, walking the double distance between this {@link Searcher} and the treasure.
     */
    @Override
    public SearchPath move(CircleHint hint) {
        SearchPath searchPath = new SearchPath(JTSUtils.createPoint(hint.getCircle().getCenter()));
        searchPath.addPoint(JTSUtils.createPoint(
                hint.getCircle().getCenter().getX() * 2,
                hint.getCircle().getCenter().getY() * 2

        ));
        return searchPath;
    }
}
