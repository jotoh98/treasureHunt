package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekSearcher;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
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
    public void init(Point startPosition, int width, int height) {
        currentPosition = startPosition;
    }

    /**
     * @return A {@link Movement} to the point, the user gave.
     */
    @Override
    public Movement move() {
        Point moveTo = SwingUtils.promptForPoint("Please provide a initial move location", "");
        Movement movement = new Movement(currentPosition);
        currentPosition = moveTo;
        movement.addWayPoint(moveTo);
        return movement;
    }

    /**
     * @return A {@link Movement} to the point, the user gave.
     */
    @Override
    public Movement move(Hint hint) {
        Point moveTo = SwingUtils.promptForPoint("Please provide a move location", "Hint is: " + hint);
        Movement movement = new Movement(currentPosition);
        currentPosition = moveTo;
        movement.addWayPoint(moveTo);
        return movement;
    }
}
