package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Point;

/**
 *
 */
public class RandomHalfPlaneHintHider implements Hider<HalfPlaneHint> {
    private Point treasurePos = JTSUtils.createPoint(Math.random() * 100, Math.random() * 100);

    /**
     * @param movement the {@link Movement}, the {@link com.treasure.hunt.strategy.searcher.Searcher} did last
     * @return T a (new) hint.
     */
    @Override
    public HalfPlaneHint move(Movement movement) {
        Point searcherPos = movement.getEndPoint();

        double randomAngle = Math.random() * -Math.PI; // Angle between treasurePosition searcherPosition and
        // AnglePointRight
        double rightAngle = Angle.angle(searcherPos.getCoordinate(), treasurePos.getCoordinate()) + randomAngle;
        double rightX = searcherPos.getX() + Math.cos(rightAngle);
        double rightY = searcherPos.getY() + Math.sin(rightAngle);
        return new HalfPlaneHint(searcherPos, JTSUtils.createPoint(rightX, rightY));
    }

    /**
     * @return the current treasure location
     */
    @Override
    public Point getTreasureLocation() {
        return treasurePos;
    }
}
