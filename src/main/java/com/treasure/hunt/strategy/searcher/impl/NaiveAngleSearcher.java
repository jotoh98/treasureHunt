package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekSearcher;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * This type of {@link Searcher} just always passes the middle of a given {@link AngleHint},
 * by a distance of 1.
 *
 * @author dorianreineccius
 */
public class NaiveAngleSearcher implements HideAndSeekSearcher<AngleHint> {
    private Point startPosition;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Point startPosition) {
        this.startPosition = startPosition;
    }

    /**
     * @return {@link SearchPath}, containing only the starting position.
     */
    @Override
    public SearchPath move() {
        return new SearchPath(startPosition);
    }

    /**
     * @param angleHint the hint, the {@link com.treasure.hunt.strategy.hider.Hider} gave last.
     * @return A {@link SearchPath} 1 length unit trough the middle of the AngleHint.
     */
    @Override
    public SearchPath move(AngleHint angleHint) {
        Coordinate coordinate = JTSUtils.middleOfAngleHint(angleHint);
        return new SearchPath(JTSUtils.createPoint(coordinate.x, coordinate.y));
    }
}
