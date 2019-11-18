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
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/*
    There are 3 Structures to represent the state of Algorithm
    BoundingCircle:         the biggest Area in which the Strategy wants to place the treasure
    checkedArea:            the Area the player has visited and thus must not contain the target
    AreaExcludedByHints:    the Area the previous hints have

    ==> those 3 structures are used to calculate the remaining possible area to place the treasure into
    possibleArea:           BoundingCircle \ {checkedArea + AreaExcludedByHints)

    The Algorithm tries to greedily maximize the possibleArea with each Hint generation

 */
public class MaxAreaAngularHintStrategy implements Hider<AngleHint> {

    private Point startingPoint;
    private Point currentPlayersPosition;
    private Point initTreasure;
    private GameHistory gameHistory;
    private List<AngleHint> givenHints;
    private GeometryFactory gf;

    @Getter
    private GeometryItem<Geometry> possibleArea;
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


    public MaxAreaAngularHintStrategy() {
        gf = new GeometryFactory(); //TODO swap out with externally defined default factory for project
        givenHints = new ArrayList<>();
    }

    @Override
    public void init(Point treasurePosition, GameHistory gameHistory) {
        startingPoint = gf.createPoint(new Coordinate(0, 0));
        initTreasure = treasurePosition;
        this.gameHistory = gameHistory;

        //TODO this should be easier
        Circle c = new Circle(startingPoint.getCoordinate(), boundingCircleSize, gf);
        GeometryItem<Circle> bc = new GeometryItem<>(c, GeometryType.BOUNDING_CIRCE); //this should be usable as a commitProduct() parameter
        GenericProduct gp = new GenericProduct();
        gp.addAdditionalItem(bc.getObject(), bc.getType());
        commitProduct(gp);

        possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{c}, gf), GeometryType.POSSIBLE_TREASURE);


    }


    /**
     * Computes the 2 Intersections between the bounding circle and the current hint, then
     * Merges the resulting Polygon and the remaining possible Area
     * to the new possible Area
     *
     * @param hint The hint to integrate
     */

    public void integrateHint(AngleHint hint) {

        //for each hintVector there's going to be 2 intersections with the bounding circle, Take the ones which scale the Vector with a positive factor
        LineSegment firstVector = new LineSegment(hint.getAngleCenter().getCoordinate(), hint.getAnglePointOne().getCoordinate());
        LineSegment secondVector = new LineSegment(hint.getAngleCenter().getCoordinate(), hint.getAnglePointTwo().getCoordinate());

        Coordinate[] boundingPoints = boundingCircle.getObject().getExteriorRing().getCoordinates();
        if (isClockwiseOrdered(boundingPoints) == false) {
            Coordinate[] h = new Coordinate[boundingPoints.length];
            for (int i = 0; i < boundingPoints.length; i++) {
                h[boundingPoints.length - 1 - i] = boundingPoints[i];
            }
            boundingPoints = h;
        }
        LineSegment boundingSeg = new LineSegment();
        Coordinate intersects[] = new Coordinate[2];
        int[] interSectionIndex = new int[2];
        LineSegment intersectionSeg[] = new LineSegment[2];
        RobustLineIntersector intersectorCalc = new RobustLineIntersector();

        //helperstructure

        Coordinate[] loopingCoords = Stream.concat(Arrays.stream(boundingPoints), Stream.of(boundingPoints[0])).toArray(Coordinate[]::new);

        for (int curr = 0; curr < loopingCoords.length - 1; curr++) {
            boundingSeg.p0 = loopingCoords[curr];
            boundingSeg.p1 = loopingCoords[curr + 1];

            //first vector
            intersectorCalc.computeIntersection(loopingCoords[curr], loopingCoords[curr + 1], firstVector.p0, firstVector.p1);
            // sanity check to also exclude Collinear lines
            if (intersectorCalc.getIntersectionNum() == LineIntersector.POINT_INTERSECTION && intersectorCalc.isInteriorIntersection((0))) {
                //not  sure if the IntersectionIndex should actually be zero. Not sure why the intersection index can be 2 and still return a point
                if (firstVector.projectionFactor(intersectorCalc.getIntersection(0)) >= 0) {
                    intersects[0] = intersectorCalc.getIntersection(0);
                    intersectionSeg[0] = boundingSeg;
                    interSectionIndex[0] = curr;

                }
            }

            //second vector
            intersectorCalc.computeIntersection(loopingCoords[curr], loopingCoords[curr + 1], secondVector.p0, secondVector.p1);
            // sanity check to also exclude Collinear lines
            if (intersectorCalc.getIntersectionNum() == LineIntersector.POINT_INTERSECTION && intersectorCalc.isInteriorIntersection((0))) {
                //not  sure if the IntersectionIndex should actually be zero. Not sure why the intersection index can be 2 and still return a point
                if (secondVector.projectionFactor(intersectorCalc.getIntersection(0)) >= 0) {
                    intersects[1] = intersectorCalc.getIntersection(0);
                    intersectionSeg[1] = boundingSeg;
                    interSectionIndex[1] = curr;
                }
            }
        }

        // now merge the 2 intersections, the AngleCenter and the Bounding cicle
        // Todo find a way to get the orientation of the line and go from intersects[0] to intersects[1]
        LineString ls = boundingCircle.getObject().getExteriorRing();
        Polygon hintedArea;
        List<Coordinate> buildingList = new ArrayList<>();
        Coordinate coord = new Coordinate();
        buildingList.add(hint.getAngleCenter().getCoordinate());
        buildingList.add(intersects[0]);

        //if same intersectionSegment and right after the first intersection --> form triangle
        if (interSectionIndex[0] == interSectionIndex[1] &&
                boundingPoints[interSectionIndex[0]].distance(intersects[0]) < boundingPoints[interSectionIndex[1]].distance(intersects[1])) {
            buildingList.add(intersects[1]);

        } else {  //otherwise there is at least one Point on the boundary

            int coordIdx;
            if (interSectionIndex[0] == boundingPoints.length - 1) {
                coordIdx = 0;
            } else {
                coordIdx = interSectionIndex[0] + 1;
            }
            coord = (interSectionIndex[0] == boundingPoints.length - 1) ? boundingPoints[0] : boundingPoints[interSectionIndex[0]];
            buildingList.add(coord);

            do {
                buildingList.add(boundingPoints[coordIdx]);
                coordIdx++;
                if (coordIdx == boundingPoints.length) {
                    coordIdx = 0;
                }
            } while (coordIdx != interSectionIndex[1]);
            buildingList.add(intersects[1]);
        }
        Coordinate[] toArray = buildingList.stream().toArray(Coordinate[]::new);
        hintedArea = gf.createPolygon(toArray);

        Geometry newPossibleArea = possibleArea.getObject().union(hintedArea);


        this.possibleArea = new GeometryItem<>(newPossibleArea,GeometryType.POSSIBLE_TREASURE);


    }

    /**
     * TODO Could be factored out into util Class
     *
     * @param coords the coords to check
     * @return the orientation CCW or CW
     */
    public boolean isClockwiseOrdered(Coordinate[] coords) {
        double sum = 0;
        for (int cIndex = 0; cIndex < coords.length - 1; cIndex++) {
            sum += (coords[cIndex + 1].x - coords[cIndex].x) * (coords[cIndex + 1].y - coords[cIndex].y);
        }
        sum += (coords[0].x - coords[coords.length - 1].x) * ((coords[0].y - coords[coords.length - 1].y));

        if (sum >= 0) {
            return true;
        }
        return false;
    }

    @Override
    public void commitProduct(Product product) {
        //TODO, not sure if this is the intended way
        gameHistory.dump(product);
    }

    @Override
    public AngleHint move(Moves moves) {
        currentPlayersPosition = moves.getEndPoint().getObject();
        adaptBoundingCircle();

        //calc the visited Area by the player TODO should be done incrementally from move to move or be globally accessible
        Coordinate[] visitedCoordinates = new Coordinate[moves.getPoints().size()];

        for (int coordinateIndex = 0; coordinateIndex < moves.getPoints().size(); coordinateIndex++) {
            visitedCoordinates[coordinateIndex] = moves.getPoints().get(coordinateIndex).getObject().getCoordinate();
        }

        LineString walkedPath = gf.createLineString(visitedCoordinates);
        Polygon checkedPoly = (Polygon) walkedPath.buffer(1.0);
        checkedArea = new GeometryItem<>(checkedPoly, GeometryType.NO_TREASURE);

        AngleHint hint; //compute by maximizing the remaining possible Area after the Hint
        //integrateHint(hint);

        return null;
    }


    /*
        Extends the bounding Circle/Linestring when the player comes close to its edge
        At later stages the circle could be extended to a lineString, which only extends around the player, not the starting point
     */
    private void adaptBoundingCircle() {
        if (boundingCircle.getObject().isWithinDistance(currentPlayersPosition, boundingCircleSize)) {
            double newRadius = boundingCircleSize - boundingCircle.getObject().distance(currentPlayersPosition);
            boundingCircle = new GeometryItem<>(new Circle(startingPoint.getCoordinate(), newRadius, gf), GeometryType.BOUNDING_CIRCE);

            GenericProduct gp = new GenericProduct();
            gp.addAdditionalItem(boundingCircle.getObject(), GeometryType.BOUNDING_CIRCE);
            commitProduct(gp);

            //now recompute all the intersections of Hints and the Bounding Circle
            for (AngleHint hint : givenHints) {
                integrateHint(hint);
            }
        }
    }

}
