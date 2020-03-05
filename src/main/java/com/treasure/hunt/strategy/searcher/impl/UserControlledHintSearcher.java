package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekSearcher;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.SwingUtils;
import org.locationtech.jts.geom.Point;

/**
 * This is a type of {@link HideAndSeekSearcher},
 * which is controlled by the user.
 *
 * @author axel12
 */
public class UserControlledHintSearcher implements HideAndSeekSearcher<Hint> {
    private Point currentPosition;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Point startPosition) {
        currentPosition = startPosition;
    }

    /**
     * @return A {@link SearchPath} to the point, the user gave.
     */
    @Override
    public SearchPath move() {
        Point moveTo = SwingUtils.promptForPoint("Please provide a initial move location", "");
        SearchPath searchPath = new SearchPath(currentPosition);
        currentPosition = moveTo;
        searchPath.addPoint(moveTo);
        return searchPath;
    }

    /**
     * @return A {@link SearchPath} to the point, the user gave.
     */
    @Override
    public SearchPath move(Hint hint) {
        Point moveTo = SwingUtils.promptForPoint("Please provide a move location", "Hint is: " + hint);
        SearchPath searchPath = new SearchPath(currentPosition);
        currentPosition = moveTo;
        searchPath.addPoint(moveTo);
        return searchPath;
    }
}
