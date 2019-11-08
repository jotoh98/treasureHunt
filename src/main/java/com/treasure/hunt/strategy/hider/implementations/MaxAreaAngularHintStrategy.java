package com.treasure.hunt.strategy.hider.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.jts.Circle;
import com.treasure.hunt.strategy.GenericProduct;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import lombok.Getter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;

import java.util.ArrayList;
import java.util.List;

/*
    There are 3 Structures to represent the state of Algorithm
    BoundingCircle: the biggest Area in which the Strategy wants to place the treasure
    checkedArea: the Area the player has visited and thus must not contain the target
    possibleArea: BoundingCircle \ {checkedArea + AreaExcludedByHints)

    The Algorithm tries to greedily maximize the possibleArea with each Hint generation

 */
public class MaxAreaAngularHintStrategy implements Hider<AngleHint> {


    private Point startingPoint;
    private Point currentPlayersPosition;
    private Point initTreasure;
    private GameHistory gh;
    private List<AngleHint> givenHints;
    private GeometryFactory gf;

    @Getter
    private GeometryItem<MultiPolygon> possibleArea;
    private GeometryItem<Circle> boundingCircle;
    private GeometryItem<Polygon> checkedArea; //the area which has been visited by the player

    //Algorithm Parameters

    /*
    In order to ensure that the remaining Area for the treasure is closed the Strategy has a Bounding circle
    whose radius will only increase and ensure the Distance between
    the player and circles edge is >= boundingCircleSize AND
    the start and circle edge is >= boundingCircleSize
     */
    private double boundingCircleSize = 100.0; //



    public MaxAreaAngularHintStrategy(){
        gf = new GeometryFactory(); //TODO swap out with externally defined default factory for project
        givenHints = new ArrayList<>();
    }

    @Override
    public void init(Point treasurePosition, GameHistory gameHistory) {
        startingPoint = gf.createPoint(new Coordinate(0,0));
        initTreasure = treasurePosition;
        gh = gameHistory;

        //TODO this should be easier
        Circle c = new Circle(startingPoint.getCoordinate(),boundingCircleSize,gf);
        GeometryItem<Circle> bc = new GeometryItem<>(c, GeometryType.BOUNDING_CIRCE); //this should be usable as a commitProduct() parameter
        GenericProduct gp = new GenericProduct();
        gp.addAdditionalItem(bc.getObject(),bc.getType());
        commitProduct(gp);


        MultiPolygon pa = new MultiPolygon(new Polygon[]{c},gf);
        possibleArea = new GeometryItem<>(pa,GeometryType.POSSIBLE_TREASURE);


    }

    /*
        Computes the 2 Intersections between the bounding circle and the current hint, then
        Merges the resulting Polygon and the remaining possible Area
        to the new possible Area

     */
    public void integrateHint(AngleHint hint){
        //TODO
    }

    @Override
    public void commitProduct(Product product) {
        //TODO, not sure if this is the intended way
        gh.dump(product);
    }

    @Override
    public AngleHint move(Moves moves) {
        currentPlayersPosition = moves.getEndPoint().getObject();
        adaptBoundingCircle();

        //calc the visited Area by the player TODO should be done incrementally from move to move or be globally accessible
        Coordinate[] visitedCoordinates = new Coordinate[moves.getPoints().size()];

        for(int coordinateIndex = 0; coordinateIndex<moves.getPoints().size();coordinateIndex++){
            visitedCoordinates[coordinateIndex] = moves.getPoints().get(coordinateIndex).getObject().getCoordinate();
        }

        LineString walkedPath = gf.createLineString(visitedCoordinates);
        Polygon checkedPoly = (Polygon) walkedPath.buffer(1.0);
        checkedArea = new GeometryItem<>(checkedPoly,GeometryType.NO_TREASURE);


        AngleHint hint; //compute by maximizing the remaining possible Area after the Hint
        //integrateHint(hint);

        return null;
    }

    /*
        Extends the bounding Circle/Linestring when the player comes close to its edge
        At later stages the circle could be extended to a lineString, which only extends around the player, not the starting point
     */
    private void adaptBoundingCircle(){
        if(boundingCircle.getObject().isWithinDistance(currentPlayersPosition,boundingCircleSize)){
            double newRadius = boundingCircleSize - boundingCircle.getObject().distance(currentPlayersPosition);
            boundingCircle = new GeometryItem<>(new Circle(startingPoint.getCoordinate(), newRadius,gf), GeometryType.BOUNDING_CIRCE);


            GenericProduct gp = new GenericProduct();
            gp.addAdditionalItem(boundingCircle.getObject(),GeometryType.BOUNDING_CIRCE);
            commitProduct(gp);


            //now recompute all the intersections of Hints and the Bounding Circle
            for(AngleHint hint : givenHints){
                integrateHint(hint);
            }
        }
    }

}
