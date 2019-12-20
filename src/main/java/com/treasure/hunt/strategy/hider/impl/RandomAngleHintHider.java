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
    private Point treasurePosition = JTSUtils.createPoint(Math.random() * 200 - 100, Math.random() * 200 - 100);

    /**
     * @return {@link RandomAngleHintHider#treasurePosition}.
     */
    @Override
    public Point getTreasureLocation() {
        return treasurePosition;
    }

    /**
     * @param movement the {@link Movement}, the {@link com.treasure.hunt.strategy.searcher.Searcher} did last.
     * @return {@link AngleHint}, which is randomly chosen, but correct.
     */
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
