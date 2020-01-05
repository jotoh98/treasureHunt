package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * A type of {@link Hider}, generating randomly chosen {@link AngleHint}'s
 *
 * @author dorianreineccius
 */
public class RandomAngleHintHider implements Hider<AngleHint> {
    /**
     * Random picked {@link Point} from [0,100)x[0x100), containing the treasure location.
     */
    private Point treasurePosition;
    private int width;
    private int height;

    /**
     * @return {@link RandomAngleHintHider#treasurePosition}.
     */
    @Override
    public Point getTreasureLocation() {
        return treasurePosition;
    }

    @Override
    public void init(Point searcherStartPosition, int width, int height) {
        treasurePosition = JTSUtils.createPoint(Math.random() * width - (width / 2), Math.random() * height - (height / 2));
        this.width = width;
        this.height = height;
    }

    @Override
    public AngleHint move(Movement movement) {
        Coordinate searcherPos = movement.getEndPoint().getCoordinate();

        double randomAngle = Math.random() * 2 * Math.PI; // in [0, PI)
        double random = Math.random();
        double leftAngle = Angle.angle(searcherPos,
                treasurePosition.getCoordinate()) + random * randomAngle;
        double leftX = searcherPos.getX() + (Math.cos(leftAngle) * 1);
        double leftY = searcherPos.getY() + (Math.sin(leftAngle) * 1);
        double rightAngle = Angle.angle(searcherPos,
                treasurePosition.getCoordinate()) - (1 - random) * randomAngle;
        double rightX = searcherPos.getX() + (Math.cos(rightAngle) * 1);
        double rightY = searcherPos.getY() + (Math.sin(rightAngle) * 1);

        return new AngleHint(
                new Coordinate(rightX, rightY),
                searcherPos,
                new Coordinate(leftX, leftY)
        );
    }
}
