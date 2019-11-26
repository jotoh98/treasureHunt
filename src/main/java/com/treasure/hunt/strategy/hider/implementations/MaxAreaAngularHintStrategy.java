package com.treasure.hunt.strategy.hider.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.jts.Circle;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
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
//@Slf4j
public class MaxAreaAngularHintStrategy implements HideAndSeekHider<AngleHint> {

    private Point startingPoint;
    private Point currentPlayersPosition;
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
    private double circleExtensionDistance = 5.0; // if player is within circleExtensionDistance, the bounding Circle will be extended
    private int maxExtensions = 10;
    private int extensions = 0;
    @Setter
    private double searcherScoutRadius = 1.0;


    public MaxAreaAngularHintStrategy() {
        System.out.println("init");
        gf = new GeometryFactory(); //TODO swap out with externally defined default factory for project
        givenHints = new ArrayList<>();
        startingPoint = gf.createPoint(new Coordinate(0, 0));
        currentPlayersPosition = startingPoint;
        Coordinate py = startingPoint.getCoordinate();

        //TODO this should be easier
        Circle c = new Circle(startingPoint.getCoordinate(), boundingCircleSize, gf);
        GeometryItem<Circle> bc = new GeometryItem<>(c, GeometryType.BOUNDING_CIRCE); //this should be usable as a commitProduct() parameter



        possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{c}, gf), GeometryType.POSSIBLE_TREASURE);
    }



    /**
     * Computes the 2 Intersections between the bounding circle and the current hint, then
     * Merges the resulting Polygon and the remaining possible Area
     * to the new possible Area
     *
     * @param hint The hint to integrate
     */
    public Geometry integrateHint(AngleHint hint) {

        //for each hintVector there's going to be 2 intersections with the bounding circle, Take the ones which scale the Vector with a positive factor
        LineSegment firstVector = new LineSegment(hint.getCenter().getCoordinate(), hint.getAnglePointRight().getCoordinate());
        LineSegment secondVector = new LineSegment(hint.getCenter().getCoordinate(), hint.getAnglePointLeft().getCoordinate());

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
        // due to ccw orientation, move from intersects[0] in ascending order on the boundingPoints to intersects[1]
        LineString ls = boundingCircle.getObject().getExteriorRing();
        Polygon hintedArea;
        List<Coordinate> buildingList = new ArrayList<>();
        Coordinate coord = new Coordinate();
        buildingList.add(hint.getCenter().getCoordinate());
        buildingList.add(intersects[0]);

        //if same intersectionSegment and right after the first intersection --> form triangle
        if (interSectionIndex[0] == interSectionIndex[1] &&
                boundingPoints[interSectionIndex[0]].distance(intersects[0]) < boundingPoints[interSectionIndex[1]].distance(intersects[1])) {
            buildingList.add(intersects[1]);
            assert intersects[0] != intersects[1]; //otherwise the hint would be useless (No area or whole plane)

        } else {  //otherwise there is at least one Point on the boundary

            int coordIdx;
            if (interSectionIndex[0] == boundingPoints.length - 1) {
                coordIdx = 0;
            } else {
                coordIdx = interSectionIndex[0] + 1;
            }

            do {
                buildingList.add(boundingPoints[coordIdx]);
                // in case intersection is on an endPoint of the current BoundingSegment
                if (buildingList.get(buildingList.size() - 1) == buildingList.get(buildingList.size() - 2)) {
                    buildingList.remove((buildingList.size() - 1));
                }
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



        return newPossibleArea;


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

    /**
     * Brute Force Method to determine the best hint
     *
     * @param samples
     * @return
     */
    private AngleHint generateHint(int samples, Point origin){
        double twoPi = Math.PI * 2;

        double dX,dY;
        Point p1, p2;
        AngleHint hint;
        double area;
        Geometry resultingGeom;

        double maxArea = 0;
        Geometry maxGeometry = null;
        AngleHint maxAngle = null;


        for(int i = 0; i < samples; i++){
            dX = Math.cos(twoPi * (i/samples));
            dY = Math.sin(twoPi * (i/samples));
            p1 = gf.createPoint(new Coordinate(origin.getX() + dX, origin.getY() + dY));
            p2 = gf.createPoint(new Coordinate(origin.getX() - dX, origin.getY() - dY));

            hint = new AngleHint(p1,origin,p2);
            resultingGeom = integrateHint(hint);
            area = integrateHint(hint).getArea();
            if(area > maxArea){
                maxGeometry = resultingGeom;
                maxArea = area;
                maxAngle = hint;
            }
        }

        assert maxAngle != null;

        this.possibleArea = new GeometryItem<>(maxGeometry,GeometryType.POSSIBLE_TREASURE);
        return maxAngle;
    }


    /**
     * Returns the current Treasure Location
     * Always places it out of the agents reach until the remaining area
     * is less than 1
     *
     * @return
     */
    @Override
    public Point getTreasureLocation() {
        if(possibleArea.getObject().getArea() <= searcherScoutRadius * searcherScoutRadius * Math.PI) return currentPlayersPosition;


        Coordinate[] boundingPoints =  possibleArea.getObject().getCoordinates();

        System.out.println(currentPlayersPosition);
        Coordinate player = currentPlayersPosition.getCoordinate();

        for(int i = 0; i<boundingPoints.length;i++){
            if( boundingPoints[i].distance(player) >= searcherScoutRadius) return gf.createPoint(boundingPoints[i]);
        }
        assert false; // please never get here **praying**
        return currentPlayersPosition;
    }

    @Override
    public AngleHint move(Movement movement) {
        currentPlayersPosition = movement.getEndPoint();
        adaptBoundingCircle();

        //calc the visited Area by the player TODO should be done incrementally from move to move or be globally accessible
        Coordinate[] visitedCoordinates = new Coordinate[movement.getPoints().size()];
        for (int coordinateIndex = 0; coordinateIndex < movement.getPoints().size(); coordinateIndex++) {
            visitedCoordinates[coordinateIndex] = movement.getPoints().get(coordinateIndex).getObject().getCoordinate();
        }

        LineString walkedPath = gf.createLineString(visitedCoordinates);
        Polygon checkedPoly = (Polygon) walkedPath.buffer(1.0);
        checkedArea = new GeometryItem<>(checkedPoly, GeometryType.NO_TREASURE);


        AngleHint hint = generateHint(360, currentPlayersPosition); //compute by maximizing the remaining possible Area after the Hint over 360 sample points

        hint.addAdditionalItem(possibleArea);
        hint.addAdditionalItem(checkedArea);
        hint.addAdditionalItem(boundingCircle);

        return hint;
    }

    /**
     * Extends the bounding Circle/Linestring when the player comes close to its edge
     * At later stages the circle could be extended to a lineString, which only extends around the player, not the starting point
     *
     */
    private void adaptBoundingCircle() {
        if (boundingCircle.getObject().isWithinDistance(currentPlayersPosition, circleExtensionDistance) && extensions < maxExtensions) {
            double newRadius = boundingCircleSize - boundingCircle.getObject().distance(currentPlayersPosition);
            boundingCircle = new GeometryItem<>(new Circle(startingPoint.getCoordinate(), newRadius, gf), GeometryType.BOUNDING_CIRCE);

            //now recompute all the intersections of Hints and the Bounding Circle
            for (AngleHint hint : givenHints) {
                integrateHint(hint);
            }
            extensions++;
        }
    }

}
