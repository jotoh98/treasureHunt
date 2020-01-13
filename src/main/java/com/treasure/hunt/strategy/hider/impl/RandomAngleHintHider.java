package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * A type of {@link Hider}, generating randomly chosen {@link AngleHint}'s
 *
 * @author dorianreineccius
 */
public class RandomAngleHintHider implements Hider<AngleHint> {
    private Point treasurePosition = JTSUtils.createPoint(Math.random() * 100, Math.random() * 100);

    /**
     * @return {@link RandomAngleHintHider#treasurePosition}.
     */
    @Override
    public Point getTreasureLocation() {
        return treasurePosition;
    }

    @Override
    public void init(Point searcherStartPosition) {
    }

    @Override
    public AngleHint move(Movement movement) {
        Coordinate searcherPos = movement.getEndPoint().getCoordinate();

        return new AngleHint(
                JTSUtils.validRandomAngle(searcherPos, treasurePosition.getCoordinate(), 2 * Math.PI)
        );
    }
}
