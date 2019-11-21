package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekSearcher;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import org.locationtech.jts.geom.Point;

/**
 * The SpoiledSearcher follows an {@link CircleHint} and
 * moves always in the center;
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
