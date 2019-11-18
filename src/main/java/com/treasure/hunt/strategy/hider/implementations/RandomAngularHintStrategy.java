package com.treasure.hunt.strategy.hider.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class RandomAngularHintStrategy implements Hider<AngleHint> {
    private GeometryFactory factory;
    private Point treasure;
    private GameHistory history;

    @Override
    public Point getTreasureLocation() {
        return treasure;
    }

    /**
     * This initializes the treasure position (a,b) being of [0,1*50)x[0,1*50)
     *
     * @param gameHistory
     */
    @Override
    public void init(GameHistory gameHistory) {
        factory = new GeometryFactory();
        treasure = factory.createPoint(new Coordinate(
                Math.random() * 50,
                Math.random() * 50));
        // register Runnable to Gamehistory
        history = gameHistory;

    }

    /**
     * @param moves the moves which, contain the players current position as a Point
     * @return AngleHint a 180 degree angle hint whose orientation is counterclockwise i.e. the treasure is to the left of the vector(angleCenter,anglePointOne)
     */
    @Override
    public AngleHint move(Moves moves) {
        Point player = moves.getEndPoint().getObject();
        double x = (Math.random() * 2) - 1;
        double y = (Math.random() * 2) - 1;

        Point firstAnglePoint = factory.createPoint(new Coordinate(player.getX() + x, player.getY() + y));
        Point secondAnglePoint = factory.createPoint(new Coordinate(player.getX() - x, player.getY() - y));

        if (Angle.angleBetweenOriented(firstAnglePoint.getCoordinate(), player.getCoordinate(), treasure.getCoordinate()) > 0) {
            return new AngleHint(firstAnglePoint, player, secondAnglePoint);
        } else {
            return new AngleHint(secondAnglePoint, player, firstAnglePoint);
        }
    }
}
