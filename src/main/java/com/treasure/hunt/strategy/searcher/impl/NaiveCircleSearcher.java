package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekSearcher;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

/**
 * This type of {@link Searcher} always goes to the center of an {@link CircleHint}.
 *
 * @author dorianreineccius
 */
public class NaiveCircleSearcher implements HideAndSeekSearcher<CircleHint> {
    private Point position;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Point startPosition) {
        position = startPosition;
    }

    /**
     * @return {@link SearchPath}, containing only the starting position.
     */
    @Override
    public SearchPath move() {
        return new SearchPath(position);
    }

    /**
     * @param circleHint the hint, the {@link com.treasure.hunt.strategy.hider.Hider} gave last
     * @return {@link SearchPath}, to the center of the given {@link CircleHint}
     */
    @Override
    public SearchPath move(CircleHint circleHint) {
        return new SearchPath(circleHint.getCircle().getCenter());
    }
}
