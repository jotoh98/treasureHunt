package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPathPrototype;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

/**
 * This type of {@link Searcher} just always passes the middle of a given {@link AngleHint},
 * by a distance of 1.
 *
 * @author dorianreineccius
 */
public class NaiveAngleSearcher implements Searcher<AngleHint> {
    private Point startPosition;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Point startPosition) {
        this.startPosition = startPosition;
    }

    /**
     * @return {@link SearchPathPrototype}, containing only the starting position.
     */
    @Override
    public SearchPathPrototype move() {
        return new SearchPathPrototype(startPosition);
    }

    /**
     * @param angleHint the hint, the {@link com.treasure.hunt.strategy.hider.Hider} gave last.
     * @return A {@link SearchPathPrototype} 1 length unit trough the middle of the AngleHint.
     */
    @Override
    public SearchPathPrototype move(AngleHint angleHint) {
        return new SearchPathPrototype(JTSUtils.middleOfAngleHint(angleHint));
    }
}
