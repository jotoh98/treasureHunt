package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekSearcher;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

/**
 * This type of {@link Searcher} always goes to the center of an {@link CircleHint}.
 *
 * @author dorianreineccius
 */
public class NaiveCircleSearcher implements HideAndSeekSearcher<CircleHint> {

    private Point position;

    @Override
    public void init(Point startPosition) {
        position = startPosition;
    }

    @Override
    public Movement move() {
        Movement movement = new Movement();
        movement.addWayPoint(position);
        return movement;
    }

    /**
     * Always go to the center of the hint.
     *
     * @param circleHint the hint, the {@link com.treasure.hunt.strategy.hider.Hider} gave last.
     * @return The {@link Movement}, the searcher did.
     */
    @Override
    public Movement move(CircleHint circleHint) {
        Movement movement = new Movement();
        movement.addWayPoint(position);
        movement.addWayPoint(circleHint.getCenter());
        return movement;
    }
}
