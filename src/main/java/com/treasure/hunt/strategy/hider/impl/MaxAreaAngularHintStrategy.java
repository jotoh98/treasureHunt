package com.treasure.hunt.strategy.hider.impl;


import com.treasure.hunt.game.Move;
import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;

import java.lang.Math;

import com.treasure.hunt.utils.JTSUtils;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.geom.util.NoninvertibleTransformationException;
import org.locationtech.jts.util.GeometricShapeFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
    private List<AngleHint> givenHints = new ArrayList<>();
    ;
    private GeometryFactory gf = JTSUtils.GEOMETRY_FACTORY;
    ;
    private double walkedPathLength = 0.0;
    private List<Point> visitedPoints = new ArrayList<>();

    @Getter
    private GeometryItem<Geometry> possibleArea;
    private GeometryStyle possibleAreaStyle = new GeometryStyle(true, new Color(255, 105, 180));
    private GeometryItem<Circle> boundingCircle;
    private GeometryItem<Polygon> checkedArea; //the area which has been visited by the player
    private GeometryItem<Point> pointWithWorstConstant;

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

    }

    @Override
    public void init(Point searcherStartPosition) {
        log.info("MaxAreaAngularHintStrategy init");

        startingPoint = searcherStartPosition;

        currentPlayersPosition = startingPoint;
        Circle c = new Circle(startingPoint.getCoordinate(), boundingCircleSize, gf);
        boundingCircle = new GeometryItem<>(c, GeometryType.BOUNDING_CIRCE);

        visitedPoints.add(startingPoint);
        Movement startingMovement = new Movement(searcherStartPosition);
        integrateMovementWithCheckedArea(startingMovement);

        possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{c}, gf).difference(checkedArea.getObject()), GeometryType.POSSIBLE_TREASURE);
        this.pointWithWorstConstant = new GeometryItem<>(gf.createPoint(new Coordinate(10.0, -10)), GeometryType.WORST_CONSTANT, new GeometryStyle(true, new Color(0x800080)));
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
     * Computes the 2 intersections between the bounding Area and the current hint,
     * then merges the resulting polygon and the remaining possible area
     * to the new possible area
     *
     * @param hint The hint to integrate
     * @return TODO
     */
    private Geometry integrateHin(AngleHint hint) {

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

        for (int curr = 0; curr < boundingPoints.length - 1; curr++) {
            boundingSeg.p0 = boundingPoints[curr];
            boundingSeg.p1 = boundingPoints[curr + 1];

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

        Geometry newPossibleArea = possibleArea.getObject().intersection(hintedArea).difference(checkedArea.getObject());

        return newPossibleArea;
    }


    private Geometry integrateHint(AngleHint hint) {
        //log.debug("integrating Hint");
        GeometryAngle angle = hint.getGeometryAngle();
        /*
        LineSegment left = new LineSegment(angle.getCenter(), angle.getLeft());
        LineSegment right = new LineSegment(angle.getCenter(), angle.getLeft());

        // if hint is given from outside the BoundingCircle the max distance needed to cross the twice can be approx by 2* distTo Center + 2* BoundingCircleSize
        double lineSegLength = 2 * (Math.sqrt(2 * Math.pow(this.boundingCircleSize, 2)) + angle.getCenter().distance(this.startingPoint.getCoordinate())) + 10;

        // create Square of that dimension
        Coordinate[] squarePoints = new Coordinate[]{
                new Coordinate(lineSegLength, 0),
                new Coordinate(0, lineSegLength),
                new Coordinate(-lineSegLength, 0),
                new Coordinate(0, -lineSegLength),
                new Coordinate(lineSegLength, 0)};

        // under this construction the hintCenter is always within the Constructed Square
        //Polygon square = gf.createPolygon(squarePoints);

        double leftFraction = (lineSegLength + 10) / left.getLength();
        double rightFraction = (lineSegLength + 10) / right.getLength();
        Coordinate extLeftCoord = left.pointAlong(leftFraction);
        Coordinate extRightCoord = right.pointAlong(rightFraction);

        LineSegment extLeft = new LineSegment(angle.getCenter(), extLeftCoord);
        LineSegment extRight = new LineSegment(angle.getCenter(), extRightCoord);
        LineString leftLs = gf.createLineString(new Coordinate[]{angle.getCenter(), extLeftCoord});
        LineString rightLs = gf.createLineString(new Coordinate[]{angle.getCenter(), extRightCoord});
        log.info("left :" + extLeft);
        log.info("right :" + extRight);

//        Coordinate[] leftIntersections = square.intersection(leftLs).getCoordinates();
//        Coordinate[] rightIntersections = square.intersection(rightLs).getCoordinates();
//        int lLen = leftIntersections.length;
//        int rLen = rightIntersections.length;

        double leftIndex = -1, rightIndex = -1;
        Coordinate leftIntersect = null, rightIntersect = null;
        List<Coordinate> resultingCoords = new ArrayList<>();
        for (int idx = 0; idx < 4; idx++) {
            LineSegment boundarySegment = new LineSegment(squarePoints[idx], squarePoints[idx + 1]);
            Coordinate ltest = boundarySegment.intersection(extLeft);
            Coordinate rTest = boundarySegment.intersection(extRight);
            if (ltest != null) {
                leftIndex = idx + 0.5;
                leftIntersect = ltest;
                log.debug("left intersection Index" + leftIndex);
            }
            if (rTest != null) {
                rightIndex = idx + 0.5;
                rightIntersect = rTest;
                log.debug("right intersection Index" + rightIndex);
            }

        }
        resultingCoords.add(leftIntersect);
        if (leftIndex == rightIndex) {
            log.debug("same Segment");
            LineSegment boundarySegment = new LineSegment(squarePoints[(int) (leftIndex - 0.5)], squarePoints[(int) (leftIndex + 0.5)]);
            if (boundarySegment.projectionFactor(leftIntersect) > boundarySegment.projectionFactor(rightIntersect)) {
                resultingCoords.add(rightIntersect);
            } else {
                leftIndex -= 0.25;
            }
        }
        if (rightIndex < leftIndex) {
            for (int i = (int) Math.floor(leftIndex); i > rightIndex; i--) {
                resultingCoords.add(squarePoints[i]);

            }
            resultingCoords.add(rightIntersect);
        } else if (leftFraction > rightIndex) {
            leftIndex += 4;
            for (int i = (int) Math.floor(leftIndex); i > rightIndex; i--) {
                resultingCoords.add(squarePoints[i % 4]);
            }
        }

        resultingCoords.add(angle.getCenter());
        log.debug(resultingCoords.toString());
        //Polygon cutSquarePoly = gf.createPolygon((Coordinate[]) resultingCoords.toArray());

        //GeometryItem<Polygon> cutSquare = new GeometryItem<>(cutSquarePoly,GeometryType.BOUNDING_CIRCE);
        //hint.addAdditionalItem(cutSquare);

        //Geometry newPossibleArea = cutSquarePoly.intersection(this.boundingCircle.getObject());
        */

        double rightAngle = Angle.angle(angle.getCenter(), angle.getRight());
        double extend = angle.extend();

        //.info("right angle:" + Angle.toDegrees(rightAngle));
        //log.info("angle size: " + Angle.toDegrees(extend));

        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(gf);
        shapeFactory.setNumPoints(4);
        shapeFactory.setCentre(angle.getCenter());
        shapeFactory.setSize(boundingCircleSize * 8);
        LineString arcLine = shapeFactory.createArc(rightAngle, extend);
        GeometryItem<LineString> angleArea = new GeometryItem<>(arcLine, GeometryType.BOUNDING_CIRCE);
        hint.addAdditionalItem(angleArea);

        ArrayList<Coordinate> arCoords = new ArrayList<>(Arrays.asList(arcLine.getCoordinates()));

        arCoords.add(angle.getCenter());
        arCoords.add(arCoords.get(0));
        //log.debug("arc coords" + arCoords.toString());
        Coordinate[] arcArray = new Coordinate[arCoords.size()];
        arcArray = arCoords.toArray(arcArray);
        Polygon arc = gf.createPolygon(arcArray);
        //log.debug("arc polygon" + arc.getExteriorRing());
        //log.debug("circle polygon" + boundingCircle.getObject().getBoundary());

        Geometry newPossibleArea = possibleArea.getObject().intersection(arc).difference(this.checkedArea.getObject());
        GeometryItem<Geometry> circleIntersection = new GeometryItem<>(arc, GeometryType.OUTER_CIRCLE, new GeometryStyle(true, new Color(0x800080)));
        hint.addAdditionalItem(circleIntersection);

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
     * @param samples determines how many test are executed to determine the best Hint
     * @return
     */
    private AngleHint generateHint(int samples, Point origin) {
        log.info("Generating Hint #"+ (this.givenHints.size()+1));
        final double twoPi = Math.PI * 2;

        int numberOfFeatures = 2; // feature 0: area ; feature 1: approximation to get C high
        double[][] features = new double[samples][numberOfFeatures];

        AngleHint hint;
        Geometry resultingGeom;

        double maxArea = 0;
        AngleHint maxAngle = null; //new AngleHint(new Coordinate(origin.getX() + 1, origin.getY() + 0), origin.getCoordinate(), new Coordinate(origin.getX() - 1, origin.getY() - 0));
        Geometry maxGeometry = null;

        // if player out of bounding area use the line orthogonal to the line from player to boundingArea Center
        if (!boundingCircle.getObject().covers(origin)) {
            log.info("player not in bounding circle, giving generic hint");
            // use translation then rotation on Player Point by 90degree then translate back
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
        Point right, left;

        Coordinate maxPoint = getMaxConstantPoint();
//        try hacky move it slightly towards origin ( isn't always the right direction though)
//        AffineTransformation tranform = new AffineTransformation();
//        tranform.scale(0.9999,0.9999);
//        maxPoint = tranform.transform(maxPoint,maxPoint);

        this.pointWithWorstConstant = new GeometryItem<>(gf.createPoint(maxPoint), GeometryType.WORST_CONSTANT);

        //this.pointWithWorstConstant = new GeometryItem<>(gf.createPoint(new Coordinate(20,-50)),GeometryType.WORST_CONSTANT);
        log.info("Checking possible Hints for containment of " + this.pointWithWorstConstant.getObject());

        for (int i = 0; i < samples; i++) {
            double angle = twoPi * (((double) i) / samples);
            dX = Math.cos(angle);
            dY = Math.sin(angle);
            right = gf.createPoint(new Coordinate(origin.getX() + dX, origin.getY() + dY));
            left = gf.createPoint(new Coordinate(origin.getX() - dX, origin.getY() - dY));

            hint = new AngleHint(right.getCoordinate(), origin.getCoordinate(), left.getCoordinate());
            resultingGeom = integrateHint(hint);

            features[i][0] = resultingGeom.getArea();

            // get the largest area, but only if the Geometry contains the Point with the best constant
            if (features[i][0] > maxArea) {

                log.info("Containment Test on " + resultingGeom);
                log.info(" contains: " + resultingGeom.contains(pointWithWorstConstant.getObject()));
                log.info(" covers: " + resultingGeom.covers(pointWithWorstConstant.getObject()));
                log.info(" buffer contains: " + resultingGeom.buffer(0.0001).contains(pointWithWorstConstant.getObject()));
                log.info(" buffer covers: " + resultingGeom.buffer(0.0001).covers(pointWithWorstConstant.getObject()));
                log.info("boundary " + resultingGeom.getBoundary().intersects(pointWithWorstConstant.getObject()));
                log.info("distance" + resultingGeom.distance(pointWithWorstConstant.getObject()));
                log.info("inview" + hint.getGeometryAngle().inView(pointWithWorstConstant.getObject().getCoordinate()));
                //TODO rework

                if (hint.getGeometryAngle().inView(pointWithWorstConstant.getObject().getCoordinate())) { // c

                    log.info("area contains Worst Constant Point --> viable hint");
                    //log.debug("containment: " + resultingGeom.contains(this.pointWithWorstConstant.getObject()) + ", coverage: " + resultingGeom.covers(gf.createPoint(new Coordinate(99.87961816680493, 2.450428508239015))));
                    maxGeometry = resultingGeom;
                    maxArea = features[(int) i][0];
                    maxAngle = hint;
                    //log.info("current max angle hint with Containtment of Worst Point: " + hint.getGeometryAngle().getRight() + " , " + hint.getGeometryAngle().getCenter() + " " + hint.getGeometryAngle().getLeft());
                }


            }
        }
        Coordinate[] pl = new Coordinate[5];
        pl[0] = new Coordinate(1.0, 1.0);
        pl[1] = new Coordinate(-1.0, 1.0);
        pl[2] = new Coordinate(-1.0, -1.0);
        pl[3] = new Coordinate(1.0, -1.0);
        pl[4] = new Coordinate(1.0, 1.0);

//        Geometry tPoly = gf.createPolygon(pl);
//        log.info("Custom Poly: " + tPoly.covers(gf.createPoint(new Coordinate(0.0, 0.0))));
//        log.info("Custom Poly2: " + tPoly.covers(gf.createPoint(new Coordinate(1.0, 0.0))));
//        log.info("Custom Poly3: " + tPoly.covers(gf.createPoint(new Coordinate(1.0, 0.5))));
//        assert maxAngle != null;
//        assert maxGeometry != null : "MaxGeometry is NULL!";
        log.info(" the maxGeometry " + maxGeometry);
        log.info(" the worst Point " + this.pointWithWorstConstant.getObject());
        log.info(" the maxGeometry covers: " + maxGeometry.buffer(0.0001).covers(pointWithWorstConstant.getObject()));
        this.possibleArea = new GeometryItem<>(maxGeometry, GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);
        log.info(possibleArea.getObject().toString());

        return maxAngle;
    }

    /**
     * Checks the edge of possibleArea for the Point which the point which results in the biggest Constant C of :  C * minPath = actualPath
     *
     * @return the Point
     */
    private Coordinate getMaxConstantPoint() {
        assert this.possibleArea.getObject().getNumGeometries() == 1 : "more than one geom";
        Coordinate[] edges = this.possibleArea.getObject().getCoordinates();
        log.info("Sampling " + edges.length + " edges in run " + visitedPoints.size());
        Pair<Coordinate, Double> bestPoint = new Pair(this.pointWithWorstConstant.getObject().getCoordinate(), this.pointWithWorstConstant.getObject().getCoordinate().distance(this.currentPlayersPosition.getCoordinate()) / this.pointWithWorstConstant.getObject().getCoordinate().distance(this.startingPoint.getCoordinate()));
        Pair<Coordinate, Double> bestCalc = new Pair(this.pointWithWorstConstant.getObject().getCoordinate(), this.pointWithWorstConstant.getObject().getCoordinate().distance(this.currentPlayersPosition.getCoordinate()) / this.pointWithWorstConstant.getObject().getCoordinate().distance(this.startingPoint.getCoordinate()));
        Pair<Coordinate, Double> bestPointOnEdge;
        Pair<Coordinate, Double> calculatedPoint;

        for (int currentCoordinateIndex = 1; currentCoordinateIndex < edges.length; currentCoordinateIndex++) {
            calculatedPoint = calcMaxConstantPointOnSegment(edges[currentCoordinateIndex - 1], edges[currentCoordinateIndex], this.currentPlayersPosition.getCoordinate());
            bestPointOnEdge = sampleMaxConstantPointOnLineSegment(edges[currentCoordinateIndex - 1], edges[currentCoordinateIndex], this.currentPlayersPosition.getCoordinate());

            if (bestPointOnEdge.getValue() > bestPoint.getValue()) {
                bestPoint = bestPointOnEdge;
            }
            if (calculatedPoint.getValue() > bestCalc.getValue()) {
                bestCalc = bestPointOnEdge;
            }
        }
        calcMaxConstantPointOnSegment(new Coordinate(-6.0, 8.0), new Coordinate(8.0, 4.0), this.currentPlayersPosition.getCoordinate());
        //calculatedPoint = calcMaxConstantPointOnSegment(edges[edges.length - 1], edges[0], this.currentPlayersPosition.getCoordinate());

        log.info("best overall Sampled constant is" + bestPoint.getValue() + " at " + bestPoint.getKey().toString());
        log.info("best overall calculated constant is" + bestPoint.getValue() + " at " + bestPoint.getKey().toString());
        return bestCalc.getKey();
    }

    /**
     * Alternate to {@sampleMaxConstantPointOnLineSegment}
     * Calculates the Point on the boundarySegment described by p1,p2 of the possible Area which results in the largest Constant of PathMin * C = PathActual
     *
     * @param p1     the Start of the Segment v
     * @param p2     the End of the Segment v
     * @param player current Position of the Player
     * @return the Point and its corresponding constant C
     */
    private Pair<Coordinate, Double> calcMaxConstantPointOnSegment(Coordinate p1, Coordinate p2, Coordinate player) {
        LineSegment unscaled = new LineSegment(p1, p2);
        //log.debug("Calculating on " + unscaled);
        Point origin = gf.createPoint(new Coordinate(0, 0)); // origin assumed to be (0,0)
        assert origin.equals(this.startingPoint);

        Coordinate projection = unscaled.project(origin.getCoordinate());
        double scalingFactor = 1.0 / projection.distance(origin.getCoordinate());

        AffineTransformation scalingTransform = AffineTransformation.scaleInstance(scalingFactor, scalingFactor);

        //determine rotation angle to put ProjectionPoint onto the Y-Axis
        double rotationAngle = Angle.angleBetweenOriented(projection, origin.getCoordinate(), new Coordinate(0, 1));

        AffineTransformation rotationTransform = AffineTransformation.rotationInstance(rotationAngle);

        Coordinate p1Transformed = p1.copy();
        Coordinate p2Transformed = p2.copy();
        Coordinate playerTransformed = player.copy();

        //log.debug("scaling by Factor" + scalingFactor);
        p1Transformed = scalingTransform.transform(p1, p1Transformed);
        p2Transformed = scalingTransform.transform(p2, p2Transformed);
        playerTransformed = scalingTransform.transform(player, playerTransformed);
        //log.info("after scaling: " + new LineSegment(p1Transformed, p2Transformed));
        //log.info(playerTransformed.toString());

        //log.info("rotation by Angle" + Angle.toDegrees(rotationAngle));
        p1Transformed = rotationTransform.transform(p1Transformed, p1Transformed);
        p2Transformed = rotationTransform.transform(p2Transformed, p2Transformed);
        playerTransformed = rotationTransform.transform(playerTransformed, playerTransformed);

        LineSegment normalizedLineSegment = new LineSegment(p1Transformed, p2Transformed);
        //log.info("final " + normalizedLineSegment);
        //log.info("Projection Point on LS: " + normalizedLineSegment.project(origin.getCoordinate()));

        //now normal state is reached and Derivative can be applied for MaxConstant Search

        //log.info("player is now on " + playerTransformed);
        double a = playerTransformed.x;
        double b = playerTransformed.y;
        double maximum, extremum1, extremum2, variableTerm;

        if (a != 0.0) { //player is not on Y-Axis --> 2 Extrema
            //log.info(" player NOT on Y-Axis");
            variableTerm = Math.sqrt(Math.pow(-2 * Math.pow(a, 2.0) - 2 * Math.pow(b, 2.0) + 4 * b, 2.0) + 16 * Math.pow(a, 2.0));

            extremum1 = (variableTerm + 2 * Math.pow(a, 2.0) + 2 * Math.pow(b, 2.0) - 4 * b) / (4 * a);
            extremum2 = (-variableTerm + 2 * Math.pow(a, 2.0) + 2 * Math.pow(b, 2.0) - 4 * b) / (4 * a);
            //log.info(" extremum1 : " + extremum1);
            //log.info(" extremum2 : " + extremum2);
            //now plug into 2nd Derivative to see who is Max and whos Min
            if (evalSecondDerivOfConstFunction(extremum1, a, b) < 0) {
                //log.info("Extremum 1 is a maxima");
                maximum = extremum1;
            } else if (evalSecondDerivOfConstFunction(extremum2, a, b) < 0) {
                //log.info("Extremum 2 is a maxima");
                maximum = extremum2;
            } else {
                //log.info("This should not be possible");
                maximum = 0.0;
            }
            //SavetyCheck nessecary here, but long formula .....

        } else { // either both points are Identical( x can be chosen arbitrarily), or x == 0
            //log.info(" player on Y-Axis");
            if (b == 0.0 || b == 2.0) { // all points on line have the same constant ==> choose x=0
                maximum = 0;
            } else if (b > 0 || b > 2) { // 0 maximizes the Constant
                maximum = 0;

            } else { //  0 < b < 2 --> 0 minimizes the Constant --> choose the Endpoint of the segment, which is further away
                maximum = Math.abs(p1Transformed.x) > Math.abs(p2Transformed.x) ? p1Transformed.x : p2Transformed.x;
            }
        }
        //log.info("Calculated Maximum on Line is Point : " + maximum + " , 1");

        // check if maximum is on LineSegment
        double left = Math.min(p1Transformed.x, p2Transformed.x);
        double right = Math.max(p1Transformed.x, p2Transformed.x);
        double leftConstant = evaluateConstantFunction(playerTransformed, new Coordinate(left, 1), origin.getCoordinate());
        double rightConstant = evaluateConstantFunction(playerTransformed, new Coordinate(right, 1), origin.getCoordinate());

        if (maximum < left || maximum > right) { //if outside the LineSegment pick the better of the edgePoints
            if (leftConstant > rightConstant) {
                maximum = left;
            } else {
                maximum = right;
            }
        } else { // if inside the LineSegment pick the better out of the Max and the Two edgePoints
            double maximumConstant = evaluateConstantFunction(playerTransformed, new Coordinate(maximum, 1), origin.getCoordinate());

            if (maximumConstant > leftConstant && maximumConstant > rightConstant) {
                maximum = maximum;
            } else if (leftConstant > maximumConstant && leftConstant > rightConstant) {
                maximum = left;
            } else {
                maximum = right;
            }
        }
        //log.info("Calculated Maximum on LineSEGMENT is Point : " + maximum + " , 1");
        Coordinate bestCoordinate = new Coordinate(maximum, 1.0);
        // Now Transform that Point back and/or Calc the Angle which it has
        try {
            bestCoordinate = rotationTransform.getInverse().transform(bestCoordinate, bestCoordinate);
            bestCoordinate = scalingTransform.getInverse().transform(bestCoordinate, bestCoordinate);

            //bit hacky but needed to make bestCoordinate lie on the LineString
            //log.info("Distance from " + bestCoordinate + "to " + unscaled + " : " + unscaled.distance(bestCoordinate)); // are in the range of 10e-16
            bestCoordinate = unscaled.project(bestCoordinate);
            //log.info("after projection; Distance from " + bestCoordinate + "to " + unscaled + " : " + unscaled.distance(bestCoordinate)); // stays in the 10e-16 range

        } catch (NoninvertibleTransformationException e) {
            //log.info("Non invertible, but it should be!");
            e.printStackTrace();
        }
        //log.info("Final Point after InverseTransformation: " + bestCoordinate);

        return new Pair<>(bestCoordinate, evaluateConstantFunction(playerTransformed, bestCoordinate, origin.getCoordinate()));

    }

    private double evaluateConstantFunction(Coordinate player, Coordinate pointToEvaluate, Coordinate origin) {
        return player.distance(pointToEvaluate) / origin.distance(pointToEvaluate);
    }

    private double evalSecondDerivOfConstFunction(double x, double a, double b) {
        double firstTerm = (8 * x * (Math.pow(a, 2.0) * x - a * Math.pow(x, 2.0) + a + (b - 2) * b * x)) / Math.pow(Math.pow(x, 2.0) + 1, 3);
        double secondTerm = (2 * (Math.pow(a, 2.0) - 2 * a * x + (b - 2) * b)) / Math.pow(Math.pow(x, 2.0) + 1, 2.0);
        return firstTerm - secondTerm;
    }

    /**
     * Samples for the Point on the boundarySegment described by p1,p2 of the possible Area which results in the largest Constant of PathMin * C = PathActual
     *
     * @param p1     the Start of the Segment v
     * @param p2     the End of the Segment v
     * @param player current Position of the Player
     * @return the Point and its corresponding constant C
     */
    private Pair<Coordinate, Double> sampleMaxConstantPointOnLineSegment(Coordinate p1, Coordinate p2, Coordinate player) {
        //log.info("checking Linesegment(" + p1.getX() + ", " + p1.getY() + ") -> (" + p2.getX() + ", " + p2.getY() + ")");
        double eps = 0.2; //the maximum distance between two sampled Point on the LineSegment

        LineSegment segment = new LineSegment(p1, p2);
        double length = segment.getLength();
        int samples = (int) Math.ceil(length / eps);

        Pair<Coordinate, Double> bestPoint = new Pair(p1, p1.distance(this.currentPlayersPosition.getCoordinate()) / p1.distance(startingPoint.getCoordinate()));
        for (int step = 1; step <= samples; step++) {

            double fractionAlong = ((double) step) / (double) samples;
            Coordinate currentPoint = segment.pointAlong(fractionAlong);
            double distToPlayer = currentPoint.distance(this.currentPlayersPosition.getCoordinate());
            double distToStart = currentPoint.distance(startingPoint.getCoordinate());
            double constant = distToPlayer / distToStart;
            if (constant > bestPoint.getValue()) {
                bestPoint = new Pair<>(currentPoint, constant);
                //log.info("new Best Point found on Linesegment at " + step + "/" + samples + "with C=" + constant);
            }
        }

        return bestPoint;
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
        return this.pointWithWorstConstant.getObject();
    }

    private void integrateMovementWithCheckedArea(Movement movement) {
        List<Point> newMovementPoints = movement.getPoints().subList(1, movement.getPoints().size()).stream().map(p -> p.getObject()).collect(Collectors.toList());
        this.visitedPoints.addAll(newMovementPoints);
        log.info("new Points " + newMovementPoints.toString());
        log.info("total Walked Path " + visitedPoints.toString());
        Polygon checkedPoly;
        if (visitedPoints.size() > 1) {

            Coordinate[] visitedCoords = visitedPoints.stream().map(p -> p.getCoordinate()).toArray(Coordinate[]::new);
            LineString walkedPath = gf.createLineString(visitedCoords);
            checkedPoly = (Polygon) walkedPath.buffer(searcherScoutRadius + 0.1); //0.001 to just be outside the searchers radius
            this.walkedPathLength = walkedPath.getLength();
        } else {
            log.info("1 point visited so far");
            //checkedPoly = (Polygon) newMovemenPoints.get(0).buffer(searcherScoutRadius + 0.001);
            checkedPoly = (Polygon) startingPoint.buffer(searcherScoutRadius + 0.1);
        }

        checkedArea = new GeometryItem<>(checkedPoly, GeometryType.NO_TREASURE, new GeometryStyle(true, new Color(0x1E90FF)));
    }

    @Override
    public AngleHint move(Movement movement) {
        currentPlayersPosition = movement.getEndPoint();
        integrateMovementWithCheckedArea(movement);
        adaptBoundingCircle();

        AngleHint hint = generateHint(360, currentPlayersPosition); //compute by maximizing the remaining possible Area after the Hint over 360 sample points
        givenHints.add(hint);

        hint.addAdditionalItem(checkedArea);
        hint.addAdditionalItem(possibleArea);

        hint.addAdditionalItem(boundingCircle);
        hint.addAdditionalItem(pointWithWorstConstant);

        log.info("given Hint: " + hint.getGeometryAngle().getLeft() + ",  " + hint.getGeometryAngle().getCenter() + ",  " + hint.getGeometryAngle().getRight());
        log.info("whole circle area" + boundingCircle.getObject().getArea());
        log.info("possible area " + possibleArea.getObject().getArea());
        log.info("Point with worst constant (" + pointWithWorstConstant.getObject().getX() + ", " + pointWithWorstConstant.getObject().getY() + ")");
        log.info("Treasure will be placed on Point (" + this.getTreasureLocation().getX() + ", " + this.getTreasureLocation().getY() + ")");
        return hint;
    }

    /**
     * Extends the bounding Area when the player comes close to its edge or the player has exited the bounding Area
     * At later stages the circle could be extended to a lineString, which only extends around the player, not the starting point
     */
    private void adaptBoundingCircle() {

        double distToBoundary = boundingCircleSize - currentPlayersPosition.distance(startingPoint);
        if (extensions < maxExtensions) {
            while ((distToBoundary < circleExtensionDistance || !boundingCircle.getObject().contains(currentPlayersPosition))) {

                log.info("ext distance " + boundingCircle.getObject().isWithinDistance(currentPlayersPosition, circleExtensionDistance));
                log.info("containment " + !boundingCircle.getObject().contains(currentPlayersPosition));

                boundingCircleSize += boundingCircleExtensionDelta;
                log.info("extending Bounding Area by " + boundingCircleSize + "to " + boundingCircleSize);
                boundingCircle = new GeometryItem<>(new Circle(startingPoint.getCoordinate(), boundingCircleSize, gf), GeometryType.BOUNDING_CIRCE, new GeometryStyle(true, new Color(50, 205, 50)));
                possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{boundingCircle.getObject()}, gf), GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);
                //now recompute all the intersections of Hints and the Bounding Circle
                for (AngleHint hint : givenHints) {
                    possibleArea = new GeometryItem<>(integrateHint(hint), GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);

                }
                extensions++;
                distToBoundary = boundingCircleSize - currentPlayersPosition.distance(startingPoint);
            }
        }
    }

}
