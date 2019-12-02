package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
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
    public Movement move() {
        return new Movement(startPosition);
    }

    @Override
    public Movement move(CircleHint hint) {
        Movement movement = new Movement(startPosition);
        movement.addWayPoint(JTSUtils.createPoint(
                hint.getCenter().getX() * 2,
                hint.getCenter().getY() * 2

        ));
        return movement;
    }
}
