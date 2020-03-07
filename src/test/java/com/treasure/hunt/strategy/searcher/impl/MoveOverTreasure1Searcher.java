package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPathPrototype;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

/**
 * This test {@link Searcher} walks on the treasure and leaves it,
 * in the same {@link SearchPathPrototype}.
 *
 * @author dorianreineccius
 */
public class MoveOverTreasure1Searcher implements Searcher<CircleHint> {
    private Point startPosition;

    @Override
    public void init(Point startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public SearchPathPrototype move() {
        return new SearchPathPrototype(startPosition);
    }

    @Override
    public SearchPathPrototype move(CircleHint hint) {
        SearchPathPrototype searchPathPrototype = new SearchPathPrototype(hint.getCenter());
        searchPathPrototype.addPoint(JTSUtils.createPoint(
                hint.getCenter().getX() * 2,
                hint.getCenter().getY() * 2

        ));
        return searchPathPrototype;
    }
}
