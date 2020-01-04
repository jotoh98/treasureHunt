package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * @author dorianreineccius
 */
public class RandomAngleHintHider implements Hider<AngleHint> {
    private Point treasurePos = JTSUtils.createPoint(Math.random() * 100, Math.random() * 100);

    /**
     * @return {@link Point} containing treasure location of [0,100)x[0x100)
     */
    @Override
    public Point getTreasureLocation() {
        return treasurePos;
    }

    @Override
    public AngleHint move(Movement movement) {
        Coordinate searcherPos = movement.getEndPoint().getCoordinate();

        return new AngleHint(
                JTSUtils.validRandomAngle(searcherPos, treasurePos.getCoordinate(), 2 * Math.PI)
        );
    }
}
