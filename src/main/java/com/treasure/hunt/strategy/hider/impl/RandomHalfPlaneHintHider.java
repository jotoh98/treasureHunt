package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
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

    HalfPlaneHint lastHint = null;

    /**
     * @param searcherStartPosition the {@link Searcher} starting position,
     *                              he will initialized on.
     * @param width                 the width of the playing area
     * @param height                the height of the playing area
     */
    @Override
    public void init(Point searcherStartPosition, int width, int height) { }

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

        HalfPlaneHint newHint = new HalfPlaneHint(searcherPos.getCoordinate(), new Coordinate(rightX, rightY),
                lastHint);
        lastHint = newHint;
        return newHint;
    }

    /**
     * @return {@link Point} containing treasure location
     */
    @Override
    public Point getTreasureLocation() {
        return treasurePos;
    }
}
