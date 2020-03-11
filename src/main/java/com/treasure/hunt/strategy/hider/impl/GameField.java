package com.treasure.hunt.strategy.hider.impl;


import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;

import java.awt.*;
import java.lang.Math;

import com.treasure.hunt.utils.JTSUtils;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.geom.util.NoninvertibleTransformationException;
import org.locationtech.jts.util.GeometricShapeFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * There are 3 Structures to represent the state of Algorithm
 * Bounding Area:         the biggest Area in which the Strategy wants to place the treasure
 * checkedArea:            the Area the player has visited and thus must not contain the target
 * AreaExcludedByHints:    the Area the previous hints have
 *
 * these 3 structures are used to calculate the remaining possible area to place the treasure into
 * possibleArea:           BoundingCircle \ {checkedArea + AreaExcludedByHints)
 *
 */
@Slf4j
public class GameField {

    @Getter
    private Point startingPoint;
    private Point currentPlayersPosition;
    private List<AngleHint> givenHints = new ArrayList<>();
    private GeometryFactory gf = JTSUtils.GEOMETRY_FACTORY;
    private double walkedPathLength = 0.0;
    private List<Point> visitedPoints = new ArrayList<>();


    private GeometryItem<Geometry> possibleArea;
    private GeometryStyle possibleAreaStyle = new GeometryStyle(true, new Color(255, 105, 180));
    private GeometryItem<Circle> boundingCircle;
    private GeometryItem<Polygon> checkedArea; //the area which has been visited by the player
    private GeometryItem<Point> favoredTreasureLocation;

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

    public GameField() {

    }



    public void init(Point searcherStartPosition, Point treasureLocation) {
        log.info("GameField init");

        startingPoint = searcherStartPosition;
        currentPlayersPosition = startingPoint;
        favoredTreasureLocation = new GeometryItem<>(treasureLocation, GeometryType.WORST_CONSTANT, new GeometryStyle(true, new Color(0x800080)));

        Circle c = new Circle(startingPoint.getCoordinate(), boundingCircleSize, gf);
        boundingCircle = new GeometryItem<>(c, GeometryType.BOUNDING_CIRCE);

        possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{c}, gf), GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);

        visitedPoints.add(startingPoint);
        SearchPath startingPath = new SearchPath(startingPoint);
        commitPlayerMovement(startingPath);


    }

    public Geometry getPossibleArea(){
        return this.possibleArea.getObject();
    }


    /**
     * Extends the bounding Area when the player comes close to its edge or the player has exited the bounding Area
     * At later stages the circle could be extended to a lineString, which only extends around the player, not the starting point
     */
    private void adaptBoundingCircle() {

        double distToBoundary = boundingCircleSize - currentPlayersPosition.distance(startingPoint);
        if (extensions < maxExtensions) {
            while ((distToBoundary < circleExtensionDistance || !boundingCircle.getObject().contains(currentPlayersPosition))) {


                boundingCircleSize += boundingCircleExtensionDelta;
                log.info("extending Bounding Area by " + boundingCircleSize + "to " + boundingCircleSize);
                boundingCircle = new GeometryItem<>(new Circle(startingPoint.getCoordinate(), boundingCircleSize, gf), GeometryType.BOUNDING_CIRCE, new GeometryStyle(true, new Color(50, 205, 50)));
                possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{boundingCircle.getObject()}, gf), GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);

                //now recompute all the intersections of Hints and the Bounding Circle
                for (AngleHint hint : givenHints) {
                    possibleArea = new GeometryItem<>(testHint(hint), GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);

                }
                extensions++;
                distToBoundary = boundingCircleSize - currentPlayersPosition.distance(startingPoint);
            }
        }
    }

    /**
     * Updates the GameField's state with a new Players SearchPath
     *
     * @param searchPath the new SearchPath
     */
    public void commitPlayerMovement(SearchPath searchPath) {
        currentPlayersPosition = searchPath.getLastPoint();
        adaptBoundingCircle();


        List<Point> newMovementPoints = searchPath.getPoints().subList(1, searchPath.getPoints().size());

        //int prevNumberOfPoints = visitedPoints.size();
        log.debug("supplied searchpath " + searchPath.getPoints());
        log.debug("new points to add" + newMovementPoints);
        this.visitedPoints.addAll(newMovementPoints);

        log.debug("total Walked Path " + visitedPoints.toString());
        Polygon checkedPoly;
        if (visitedPoints.size() > 1) {

            Coordinate[] visitedCoords = visitedPoints.stream().map(p -> p.getCoordinate()).toArray(Coordinate[]::new);
            LineString walkedPath = gf.createLineString(visitedCoords);
            checkedPoly = (Polygon) walkedPath.buffer(searcherScoutRadius + 0.1); //0.1 to just be outside the searchers radius
            this.walkedPathLength = walkedPath.getLength();
        } else {
            log.debug("1 point visited so far");
            //checkedPoly = (Polygon) newMovemenPoints.get(0).buffer(searcherScoutRadius + 0.001);
            checkedPoly = (Polygon) startingPoint.buffer(searcherScoutRadius + 0.1);
        }

        Geometry possible = possibleArea.getObject().difference(checkedPoly);
        checkedArea = new GeometryItem<>(checkedPoly, GeometryType.NO_TREASURE, new GeometryStyle(true, new Color(0x1E90FF)));
        possibleArea = new GeometryItem<>(possible, GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);
    }



    /**
      * Integrates the 2 intersections between the bounding circle and the current hint,
      * then merges the resulting polygon and the remaining possible area
      * to the new possible area.
     *
      * The resulting area is NOT committed, therefore this method can be used to test a possibleHint for its result
      *
      * @param hint The hint to integrate
      * @return the resulting Geometry
      **/
    public Geometry testHint(AngleHint hint) {

        GeometryAngle angle = hint.getGeometryAngle();


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


        Geometry newPossibleArea = possibleArea.getObject().intersection(arc).difference(this.checkedArea.getObject());
        GeometryItem<Geometry> circleIntersection = new GeometryItem<>(arc, GeometryType.OUTER_CIRCLE, new GeometryStyle(true, new Color(0x800080)));

        // now fill Hint with HelperStructs
        hint.addAdditionalItem(circleIntersection);
        hint.addAdditionalItem(checkedArea);
        hint.addAdditionalItem(new GeometryItem<>(newPossibleArea, GeometryType.POSSIBLE_TREASURE, possibleAreaStyle));
        hint.addAdditionalItem(boundingCircle);

        return newPossibleArea;

    }

    /**
     * Finally commits the hint
     *
     * @param hint the Hint to be integrated
     * @return the resulting Area in which the Treasure could be
     */
    public Geometry commitHint(AngleHint hint){
        Geometry newPossibleArea = testHint(hint);
        this.possibleArea = new GeometryItem<>(newPossibleArea, GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);
        return newPossibleArea;
    }

    public boolean isWithinGameField(Point p){
        return boundingCircle.getObject().covers(p);
    }


    /**
     * Checks all edges of the possibleArea for the Coordinate which maximizes the value of {dist(C - Player) / dist( C -Origin) }
     *
     * @return the List of Pairs made of (Coordinates C ; their associated Value of {dist(C-Player)/dist(C-Origin)} )
     */
    public List<Pair<Coordinate, Double>> getWorstPointsOnAllEdges() {
        assert this.possibleArea.getObject().getNumGeometries() == 1 : "more than one geom";
        Coordinate[] edges = this.possibleArea.getObject().getCoordinates();
        log.info("Sampling " + edges.length + " edges in run " + visitedPoints.size());
        Pair<Coordinate, Double> bestSampled = new Pair(this.favoredTreasureLocation.getObject().getCoordinate(), this.favoredTreasureLocation.getObject().getCoordinate().distance(this.currentPlayersPosition.getCoordinate()) / this.favoredTreasureLocation.getObject().getCoordinate().distance(this.startingPoint.getCoordinate()));
        Pair<Coordinate, Double> bestCalc = new Pair(this.favoredTreasureLocation.getObject().getCoordinate(), this.favoredTreasureLocation.getObject().getCoordinate().distance(this.currentPlayersPosition.getCoordinate()) / this.favoredTreasureLocation.getObject().getCoordinate().distance(this.startingPoint.getCoordinate()));
        Pair<Coordinate, Double> sampledOnEdge;
        Pair<Coordinate, Double> calcOnEdge;

        List<Pair<Coordinate, Double>> worstEdgePoints = new ArrayList<>();

        for (int currentCoordinateIndex = 1; currentCoordinateIndex < edges.length; currentCoordinateIndex++) {
            calcOnEdge = calcMaxConstantPointOnSegment(edges[currentCoordinateIndex - 1], edges[currentCoordinateIndex], this.currentPlayersPosition.getCoordinate());
            worstEdgePoints.add(calcOnEdge);
            sampledOnEdge = sampleMaxConstantPointOnLineSegment(edges[currentCoordinateIndex - 1], edges[currentCoordinateIndex], this.currentPlayersPosition.getCoordinate());
            log.info("line (" + edges[currentCoordinateIndex - 1] + ", " + edges[currentCoordinateIndex] + ")");
            log.info("Calc " + calcOnEdge);
            log.info("Samp " + sampledOnEdge);
            if (sampledOnEdge.getValue() > bestSampled.getValue()) {
                bestSampled = sampledOnEdge;
            }
            if (calcOnEdge.getValue() > bestCalc.getValue()) {
                bestCalc = calcOnEdge;
                log.debug("new best Const " + bestCalc.getValue() + " at " + bestCalc.getKey() + " at line (" + edges[currentCoordinateIndex - 1] + ", " + edges[currentCoordinateIndex] + ")");
            }
        }

        worstEdgePoints.sort(new Comparator<Pair<Coordinate, Double>>() {
            @Override
            public int compare(Pair<Coordinate, Double> coordinateDoublePair, Pair<Coordinate, Double> t1) {
                return coordinateDoublePair.getValue().compareTo(t1.getValue());
            }
        }.reversed());
        log.info("worst Point List:" + worstEdgePoints.size() + "first element: " + worstEdgePoints.get(0));
        log.info("best overall Sampled constant is" + bestSampled.getValue() + " at " + bestSampled.getKey().toString());
        log.info("best overall calculated constant is" + bestCalc.getValue() + " at " + bestCalc.getKey().toString());
        return worstEdgePoints;
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
        Point origin = startingPoint;
        assert origin.equals( gf.createPoint(new Coordinate(0, 0))); // origin assumed to be (0,0)

        Coordinate projection = unscaled.project(origin.getCoordinate());
        double scalingFactor = 1.0 / projection.distance(origin.getCoordinate());

        AffineTransformation scalingTransform = AffineTransformation.scaleInstance(scalingFactor, scalingFactor);

        //determine rotation angle to put ProjectionPoint onto the Y-Axis
        double rotationAngle = Angle.angleBetweenOriented(projection, origin.getCoordinate(), new Coordinate(0, 1));

        AffineTransformation rotationTransform = AffineTransformation.rotationInstance(rotationAngle);

        Coordinate p1Transformed = p1.copy();
        Coordinate p2Transformed = p2.copy();
        Coordinate playerTransformed = player.copy();

        //log.trace("scaling by Factor" + scalingFactor);
        p1Transformed = scalingTransform.transform(p1Transformed, p1Transformed);
        p2Transformed = scalingTransform.transform(p2Transformed, p2Transformed);
        playerTransformed = scalingTransform.transform(playerTransformed, playerTransformed);
        //log.info("after scaling: " + new LineSegment(p1Transformed, p2Transformed));
        //log.info(playerTransformed.toString());

        //log.trace("rotation by Angle" + Angle.toDegrees(rotationAngle));
        p1Transformed = rotationTransform.transform(p1Transformed, p1Transformed);
        p2Transformed = rotationTransform.transform(p2Transformed, p2Transformed);
        playerTransformed = rotationTransform.transform(playerTransformed, playerTransformed);

        LineSegment normalizedLineSegment = new LineSegment(p1Transformed, p2Transformed);


        //now normal state is reached and Derivative can be applied for MaxConstant Search

        //log.trace("transformed player is now on " + playerTransformed);
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
            //SafetyCheck necessary here, but long formula .....

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
        } else { // if inside the LineSegment pick the best out of the Max and the Two edgePoints
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
        log.info("Final Point after InverseTransformation: " + bestCoordinate);
        log.info("player pos:" + player);
        log.info("origin pos " + origin.getCoordinate());
        return new Pair<>(bestCoordinate, evaluateConstantFunction(player, bestCoordinate, origin.getCoordinate()));

    }

    private double evaluateConstantFunction(Coordinate player, Coordinate pointToEvaluate, Coordinate origin) {
        return player.distance(pointToEvaluate) / origin.distance(pointToEvaluate);
    }

    private double evalSecondDerivOfConstFunction(double x, double a, double b) {
        double firstTerm = (8 * x * (Math.pow(a, 2.0) * x - a * Math.pow(x, 2.0) + a + (b - 2) * b * x)) / Math.pow(Math.pow(x, 2.0) + 1, 3);
        double secondTerm = (2 * (Math.pow(a, 2.0) - 2 * a * x + (b - 2) * b)) / Math.pow(Math.pow(x, 2.0) + 1, 2.0);
        return firstTerm - secondTerm;
    }


    // TODO cleanup
    /*
    public AngleHint move(SearchPath searchPath) {

        integrateMovementWithCheckedArea(searchPath);


        AngleHint hint = generateHint(360, currentPlayersPosition); //compute by maximizing the remaining possible Area after the Hint over 360 sample points
        givenHints.add(hint);


        hint.addAdditionalItem(favoredTreasureLocation);

        log.info("given Hint: " + hint.getGeometryAngle().getLeft() + ",  " + hint.getGeometryAngle().getCenter() + ",  " + hint.getGeometryAngle().getRight());
        log.info("whole circle area" + boundingCircle.getObject().getArea());
        log.info("possible area " + possibleArea.getObject().getArea());
        log.info("Point with worst constant (" + favoredTreasureLocation.getObject().getX() + ", " + favoredTreasureLocation.getObject().getY() + ")");
        log.info("Treasure will be placed on Point (" + this.getTreasureLocation().getX() + ", " + this.getTreasureLocation().getY() + ")");
        return hint;
    }
    */


    /**
     * TODO: get out of WIP, maybe just throw away
     * Helper method to fix some instability issues; still WIP
     * RobustLineIntersector does not recognize a point along a segment to be on the segment...
     * <p>
     * computes the intersection between a Ray / Half-Line  and a segment, afterwards projects it onto the segment vector if sufficiently close
     * so that the LineIntersector recognizes the intersection to be on the segment argument
     *
     * @param line    interpreted as Line
     * @param segment interpreted as Segment
     * @return intersection / null if no intersection exists
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

    // TODO cleanup
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



}
