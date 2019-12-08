package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

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

        // generate angle
        double randomAngle = Math.random() * 2 * Math.PI; // in [0, PI)
        double random = Math.random();
        double leftAngle = Angle.angle(searcherPos,
                treasurePos.getCoordinate()) + random * randomAngle;
        double leftX = searcherPos.getX() + (Math.cos(leftAngle) * 1);
        double leftY = searcherPos.getY() + (Math.sin(leftAngle) * 1);
        double rightAngle = Angle.angle(searcherPos,
                treasurePos.getCoordinate()) - (1 - random) * randomAngle;
        double rightX = searcherPos.getX() + (Math.cos(rightAngle) * 1);
        double rightY = searcherPos.getY() + (Math.sin(rightAngle) * 1);

        /*double angleHintToTreasure = angleBetweenOriented(treasureLocation.getCoordinate(), middle.getCoordinate(), angleLeft.getCoordinate());
        if (angleHintToTreasure > angle || angleHintToTreasure < 0) {
            throw new UserControlledAngleHintHider.WrongAngleException("Treasure  Location not contained in angle");
        }*/

        return new AngleHint(
                searcherPos,
                new Coordinate(leftX, leftY),
                new Coordinate(rightX, rightY)
        );
    }
}
