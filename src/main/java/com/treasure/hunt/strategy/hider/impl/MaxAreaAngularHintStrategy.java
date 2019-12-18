package com.treasure.hunt.strategy.hider.impl;


import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.geom.Circle;
import com.treasure.hunt.geom.GeometryAngle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;

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
@Slf4j
public class MaxAreaAngularHintStrategy implements HideAndSeekHider<AngleHint> {

    private Point startingPoint;
    private Point currentPlayersPosition;
    private List<AngleHint> givenHints;
    private GeometryFactory gf;
    private double walkedPathLength;

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
    private double boundingCircleSize = 100.0; // starting size and extensionDelta
    private double boundingCircleExtensionDelta = 100;
    private double circleExtensionDistance = 5.0; // if player is within circleExtensionDistance, the bounding Circle will be extended
    private int maxExtensions = 10;
    private int extensions = 0;
    @Setter
    private double searcherScoutRadius = 1.0;


    public MaxAreaAngularHintStrategy() {
        log.info("MaxAreaAngularHintStrategy init");
        gf = new GeometryFactory(); //TODO swap out with externally defined default factory for project
        givenHints = new ArrayList<>();
        startingPoint = gf.createPoint(new Coordinate(0, 0));
        currentPlayersPosition = startingPoint;
        Coordinate py = startingPoint.getCoordinate();

        Circle c = new Circle(startingPoint.getCoordinate(), boundingCircleSize, gf);

        boundingCircle = new GeometryItem<>(c, GeometryType.BOUNDING_CIRCE);
        possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{c}, gf), GeometryType.POSSIBLE_TREASURE);


    }

    /**
     * TODO: get out of WIP, fix return text
     * Helper method to fix some instability issues; still WIP
     * RobustLineIntersector does not recognize a point along a segment to be on the segment...
     * <p>
     * computes the intersection between a Ray / Half-Line  and a segment, afterwards projects it onto the segment vector if sufficiently close
     * so that the LineIntersector recognizes the intersection to be on the segment argument
     *
     * @param line    interpreted as Line
     * @param segment interpreted as Segment
     * @return intersection
     */
    public Coordinate computeIntersectionOnBoundary(LineSegment line, LineSegment segment) {
        double eps = 0.0000000001;
        Coordinate intersection = line.lineIntersection(segment);
        if (intersection == null) {
            return null;
        }
        if (line.projectionFactor(intersection) < 0) {
            return null;
        }
        if (intersection.distance(segment.p0) < eps) {

            return segment.p0;
        }
        if (intersection.distance(segment.p1) < eps) {

            return segment.p1;
        }
        if (segment.distance(intersection) < eps) {
            return intersection; // segment.closestPoint(intersection);
        }

        return null;
    }


    /**
     * Computes the 2 Intersections between the bounding circle and the current hint, then
     * Merges the resulting Polygon and the remaining possible Area
     * to the new possible Area
     *
     * @param hint The hint to integrate
     * @return TODO
     */
    public Geometry integrateHint(AngleHint hint) {

        //for each hintVector there's going to be 2 intersections with the bounding circle, Take the ones which scale the Vector with a positive factor
        GeometryAngle geometryAngle = hint.getGeometryAngle();
        LineSegment firstVector = new LineSegment(geometryAngle.getCenter(), geometryAngle.getRight());
        LineSegment secondVector = new LineSegment(geometryAngle.getCenter(), geometryAngle.getLeft());

        Coordinate[] boundingPoints = boundingCircle.getObject().getExteriorRing().getCoordinates();
        if (!isClockwiseOrdered(boundingPoints)) {
            Coordinate[] h = new Coordinate[boundingPoints.length];
            for (int i = 0; i < boundingPoints.length; i++) {
                h[boundingPoints.length - 1 - i] = boundingPoints[i];
            }
            boundingPoints = h;
        }
        LineSegment boundingSeg = new LineSegment();
        Coordinate[] intersects = new Coordinate[2];
        int[] interSectionIndex = new int[2];

        //TODO: refactor plz
        LineSegment[] intersectionSeg = new LineSegment[2];
        RobustLineIntersector intersectCalc = new RobustLineIntersector();

        //helperstructure

        Coordinate[] loopingCoords = Stream.concat(Arrays.stream(boundingPoints), Stream.of(boundingPoints[0])).toArray(Coordinate[]::new);

        for (int curr = 0; curr < loopingCoords.length - 1; curr++) {
            boundingSeg.p0 = loopingCoords[curr];
            boundingSeg.p1 = loopingCoords[curr + 1];

            //first vector
            Coordinate intersection = computeIntersectionOnBoundary(firstVector, boundingSeg);

            if (intersection != null) {

                intersects[0] = intersection;
                intersectionSeg[0] = boundingSeg;
                interSectionIndex[0] = curr;

            }

            //second vector
            intersection = computeIntersectionOnBoundary(secondVector, boundingSeg);

            if (intersection != null) {

                intersects[1] = intersection;
                intersectionSeg[1] = boundingSeg;
                interSectionIndex[1] = curr;
            }

        }

        // now merge the 2 intersections, the AngleCenter and the Bounding cicle
        // due to ccw orientation, move from intersects[0] in ascending order on the boundingPoints to intersects[1]
        LineString ls = boundingCircle.getObject().getExteriorRing();
        Polygon hintedArea; // the intersection of the bounding circle and the Hint
        List<Coordinate> buildingList = new ArrayList<>();

        buildingList.add(hint.getGeometryAngle().getCenter());
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
            buildingList.add(hint.getGeometryAngle().getCenter());
        }
        Coordinate[] toArray = buildingList.stream().toArray(Coordinate[]::new);
        hintedArea = gf.createPolygon(toArray);

        Geometry newPossibleArea = possibleArea.getObject().intersection(hintedArea);

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

        return sum >= 0;
    }

    /**
     * Brute Force Method to determine the best hint by sampling over all possible angles
     *
     * @param samples determines how many test are executed to determine the max best Hint
     * @return
     */
    private AngleHint generateHint(int samples, Point origin) {
        final double twoPi = Math.PI * 2;

        AngleHint hint;
        double area;
        Geometry resultingGeom;

        double maxArea = 0;
        Geometry maxGeometry = null;
        AngleHint maxAngle = null;

        // if player out of bounding area use a use the line orthogonal to the line to the boundingArea Center
        if (!boundingCircle.getObject().contains(origin)) {
            log.info("player not in bounding circle, giving generic hint");
            // use translation then rotation on Player Point by 90degree the translate back
            //AffineTransform orthAroundPlayer = new AffineTransform(0.0, -1.0 , 2 * origin.getX() , 1.0 , 0.0, -origin.getX() + origin.getY());
            AffineTransformation orthAroundPlayer = new AffineTransformation();
            orthAroundPlayer.rotate(Math.toRadians(90), origin.getX(), origin.getY());
            Coordinate orth = new Coordinate(0.0, 0.0); //to middle of circle
            orthAroundPlayer.transform(orth, orth);

            Point right = gf.createPoint(new Coordinate(origin.getX() - (orth.x - origin.getX()), origin.getY() - (orth.y - origin.getY())));
            Point left = gf.createPoint(orth);
            AngleHint ooBHint = new AngleHint(right.getCoordinate(), origin.getCoordinate(), left.getCoordinate());
            return ooBHint;

        }

        double dX, dY;
        Point p1, p2;
        int numberOfFeatures = 2; // feature 0: area ; feature 1: approximation to get C high
        double[][] features = new double[samples][numberOfFeatures];

        for (double i = 0; i < samples; i++) {
            double angle = twoPi * (i / samples);
            dX = Math.cos(angle);
            dY = Math.sin(angle);
            p1 = gf.createPoint(new Coordinate(origin.getX() + dX, origin.getY() + dY));
            p2 = gf.createPoint(new Coordinate(origin.getX() - dX, origin.getY() - dY));

            hint = new AngleHint(p1.getCoordinate(), origin.getCoordinate(), p2.getCoordinate());

            resultingGeom = integrateHint(hint);

            features[(int) i][0] = resultingGeom.getArea();

            // calc closest Point to origin (the point which results in the biggest Constant C of :  C * minPath = actualPath )
            Coordinate[] boundary = resultingGeom.getCoordinates();

            if (features[(int) i][0] > maxArea) {
                maxGeometry = resultingGeom;
                maxArea = features[(int) i][0];
                maxAngle = hint;

            }
        }

        assert maxAngle != null;

        this.possibleArea = new GeometryItem<>(maxGeometry, GeometryType.POSSIBLE_TREASURE);
        return maxAngle;
    }

    /**
     * Calculates the Point on the boundarySegment described by p1,p2 of the possible Area which results in the largest Constant of PathMin * C = PathActual
     *
     * @param p1
     * @param p2
     * @param Player
     * @return the Point
     */
    private Point getMaxConstant(Coordinate p1, Coordinate p2, Coordinate Player) {
        return gf.createPoint(p1);
    }

    /**
     * Returns the current Treasure Location
     * Always places it out of the agents reach until the remaining area
     * is less than 1
     *
     * @return current treasure location
     */
    @Override
    public Point getTreasureLocation() {
        if (possibleArea.getObject().getArea() <= searcherScoutRadius * searcherScoutRadius * Math.PI) {
            return currentPlayersPosition;
        }

        Coordinate[] boundingPoints = possibleArea.getObject().getCoordinates();

        Coordinate player = currentPlayersPosition.getCoordinate();

        for (int i = 0; i < boundingPoints.length; i++) {
            if (boundingPoints[i].distance(player) >= searcherScoutRadius) {
                return gf.createPoint(boundingPoints[i]);
            }
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
        this.walkedPathLength = walkedPath.getLength();
        Polygon checkedPoly = (Polygon) walkedPath.buffer(1.0);
        checkedArea = new GeometryItem<>(checkedPoly, GeometryType.NO_TREASURE);

        AngleHint hint = generateHint(360, currentPlayersPosition); //compute by maximizing the remaining possible Area after the Hint over 360 sample points
        givenHints.add(hint);
        hint.addAdditionalItem(possibleArea);
        hint.addAdditionalItem(checkedArea);
        hint.addAdditionalItem(boundingCircle);
        log.info("given Hint: " + hint.getGeometryAngle().getLeft() + ",  " + hint.getGeometryAngle().getCenter() + ",  " + hint.getGeometryAngle().getRight());
        log.info("whole circle area" + boundingCircle.getObject().getArea());
        log.info("possible area " + possibleArea.getObject().getArea());
        return hint;
    }

    /**
     * Extends the bounding Area when the player comes close to its edge or the player has exited the bouding Area
     * At later stages the circle could be extended to a lineString, which only extends around the player, not the starting point
     */
    private void adaptBoundingCircle() {

        double distToBoundary = boundingCircleSize - currentPlayersPosition.distance(gf.createPoint(new Coordinate(0.0, 0.0)));
        if ((distToBoundary < circleExtensionDistance || !boundingCircle.getObject().contains(currentPlayersPosition)) && extensions < maxExtensions) {
            log.info("ext distance " + boundingCircle.getObject().isWithinDistance(currentPlayersPosition, circleExtensionDistance));
            log.info("containment " + !boundingCircle.getObject().contains(currentPlayersPosition));
            extensions++;
            boundingCircleSize += boundingCircleExtensionDelta;
            log.info("extending Bounding Area by " + boundingCircleSize + "to " + boundingCircleSize);
            boundingCircle = new GeometryItem<>(new Circle(startingPoint.getCoordinate(), boundingCircleSize, gf), GeometryType.BOUNDING_CIRCE);
            possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{boundingCircle.getObject()}, gf), GeometryType.POSSIBLE_TREASURE);
            //now recompute all the intersections of Hints and the Bounding Circle
            for (AngleHint hint : givenHints) {
                possibleArea = new GeometryItem<>(integrateHint(hint), GeometryType.POSSIBLE_TREASURE);
            }
            extensions++;
        }
    }

}
