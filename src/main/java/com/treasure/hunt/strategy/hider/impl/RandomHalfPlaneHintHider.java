package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * This type of {@link Hider} returns a random {@link HalfPlaneHint},
 * which is correct.
 *
 * @author Rank
 */
public class RandomHalfPlaneHintHider implements Hider<HalfPlaneHint> {
    double xmax = 1000;
    double ymax = 1000;
    private Point treasurePos = JTSUtils.createPoint(Math.random() * xmax * 2 - xmax,
            Math.random() * ymax * 2 - ymax);

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
        return new HalfPlaneHint(searcherPos.getCoordinate(), new Coordinate(rightX, rightY));
    }

    /**
     * @return {@link Point} containing treasure location of [0,100)x[0x100)
     */
    @Override
    public Point getTreasureLocation() {
        return treasurePos;
    }
}
