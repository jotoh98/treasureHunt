package com.treasure.hunt.strategy.hider.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import lombok.Getter;
import lombok.Setter;

public class RandomAngularHintStrategy implements Hider<AngleHint> {
    private GeometryFactory factory;
    private Point treasure;
    private GameHistory history;



    @Override
    public void init(Point treasurePosition, GameHistory gameHistory) {
        factory = new GeometryFactory();
        treasure = treasurePosition;
        // register Runnable to Gamehistory
        history = gameHistory;

    }

    @Override
    public void commitProduct(Product product) {

    }
    /**
     *
     * @param moves the moves which, contain the players current position as a Point
     * @return AngleHint a 180 degree angle hint whose orientation is counterclockwise i.e. the treasure is to the left of the vector(angleCenter,anglePointOne)
     */
    @Override
    public AngleHint move(Moves moves){
        Point player = moves.getEndPoint().getObject();
        double x = (Math.random()*2) -1;
        double y = (Math.random()*2) -1;

        Point firstAnglePoint = factory.createPoint(new Coordinate(player.getX() + x,player.getY() + y));
        Point secondAnglePoint = factory.createPoint(new Coordinate(player.getX() - x,player.getY() - y));


        if(Angle.angleBetweenOriented(firstAnglePoint.getCoordinate(),player.getCoordinate(),treasure.getCoordinate())>0){
            return new AngleHint(firstAnglePoint,player,secondAnglePoint);
        }else{
            return new AngleHint(secondAnglePoint,player,firstAnglePoint);
        }


    }


}
