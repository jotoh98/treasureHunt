package com.treasure.hunt.strategy.hider.impl;


import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.geom.util.NoninvertibleTransformationException;
import org.locationtech.jts.util.GeometricShapeFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * There are 3 Structures to represent the state of Algorithm
 * Bounding Area:         the biggest Area in which the Strategy wants to place the treasure
 * checkedArea:            the Area the player has visited and thus must not contain the target
 * AreaExcludedByHints:    the Area the previous hints have
 * <p>
 * these 3 structures are used to calculate the remaining possible area to place the treasure into
 * possibleArea:           BoundingCircle \ {checkedArea + AreaExcludedByHints)
 */

@Slf4j
public class GameField {

    public static final String CircleExtension_Preference = "Circle on MaxExtention from start? 1/0";

    @Getter
    private Point startingPoint;
    private Point currentPlayersPosition;
    private List<AngleHint> givenHints = new ArrayList<>();
    private GeometryFactory geometryFactory = JTSUtils.GEOMETRY_FACTORY;
    private double walkedPathLength = 0.0;
    private List<Point> visitedPoints = new ArrayList<>();
    private boolean treasureMovedthisTurn = false;


    private GeometryItem<Geometry> possibleArea;
    private GeometryStyle possibleAreaStyle = new GeometryStyle(true, new Color(255, 105, 180), new Color(255, 105, 180, 64));

    private GeometryItem<Circle> boundingCircle;
    private GeometryStyle boundingCircleStyle = new GeometryStyle(true, new Color(50, 205, 50));
    private GeometryItem<Polygon> checkedArea; //the area which has been visited by the player
    private GeometryStyle checkedAreaStyle = new GeometryStyle(true, new Color(0x1E90FF));
    private GeometryItem<Point> favoredTreasureLocation;
    private GeometryStyle favoredTreasureLocationStyle = new GeometryStyle(true, new Color(0x800080));
    @Getter
    private GeometryItem<Polygon> innerBufferItem;
    private GeometryStyle innerBufferStyle = new GeometryStyle(true, new Color(255, 0, 33));
    /*
    In order to ensure that the remaining Area for the treasure is closed the Strategy has a Bounding circle
    whose radius will only increase and ensure the Distance between
    the player and circles edge is >= boundingCircleSize AND
    the start and circle edge is >= boundingCircleSize
     */
    private double boundingCircleSize = 100.0; // starting size and extensionDelta
    private double boundingCircleExtensionDelta = 100;
    // if player is within circleExtensionDistance from boundingCircle, the bounding Circle will be extended
    private double circleExtensionDistance = 5.0;
    private int maxExtensions = 10;
    private int extensions = 0;
    @Setter
    private double searcherScoutRadius = 1.0;

    /**
     * Asks the Gamefield to move the treasure to the speciefied new location
     *
     * @param newTreasureLocation the new treasure location
     * @throws ImpossibleTreasureLocationException is thrown when newTreasureLocation is not within the checked area or violates a previously given hint
     */
    public void moveTreasure(Point newTreasureLocation) throws ImpossibleTreasureLocationException {
        treasureMovedthisTurn = true;
        for (AngleHint hint : givenHints) {
            if (!hint.getGeometryAngle().inView(newTreasureLocation.getCoordinate())) {
                log.debug("Hint invalid");
                throw new ImpossibleTreasureLocationException("you've supplied a treasure location, which is inconsistent with a previously given Hint: " + hint.getGeometryAngle().toString());
            }
        }
        // needed since the buffer is normally drawn with  generosity {searcherScoutRadius + 0.1}
        Coordinate[] visitedCoords = visitedPoints.stream().map(p -> p.getCoordinate()).toArray(Coordinate[]::new);
        LineString walkedPath = geometryFactory.createLineString(visitedCoords);

        if (walkedPath.buffer(searcherScoutRadius - 0.1).covers(newTreasureLocation)) {
            log.debug("Hint invalid");
            throw new ImpossibleTreasureLocationException("you've supplied a treasure location, which is inconsistent the checked area the player has already visited");
        }

        favoredTreasureLocation = new GeometryItem<>(newTreasureLocation, GeometryType.WORST_CONSTANT, favoredTreasureLocationStyle);
        log.info("new treasure location at" + favoredTreasureLocation.getObject());
    }

    /**
     * Initializes the Gamefield with its initial values
     *
     * @param searcherStartPosition the position, the player starts at
     * @param treasureLocation      the postition, the player wants to reach
     */
    public void init(Point searcherStartPosition, Point treasureLocation) {
        log.debug("GameField init");

        startingPoint = searcherStartPosition;
        currentPlayersPosition = startingPoint;
        favoredTreasureLocation = new GeometryItem<>(treasureLocation, GeometryType.WORST_CONSTANT, favoredTreasureLocationStyle);
        log.info(treasureLocation.toString());
        log.info(searcherStartPosition.toString());

        Circle circle;
        if (PreferenceService.getInstance().getPreference(GameField.CircleExtension_Preference, 0).intValue() == 1) {
            boundingCircleSize = ((maxExtensions - 1) * boundingCircleExtensionDelta) + boundingCircleSize;
            circle = new Circle(startingPoint.getCoordinate(), boundingCircleSize);
            extensions = maxExtensions;

        } else {
            circle = new Circle(startingPoint.getCoordinate(), boundingCircleSize);
        }

        boundingCircle = new GeometryItem<>(circle, GeometryType.BOUNDING_CIRCE, boundingCircleStyle);

        possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{circle.toPolygon()}, geometryFactory), GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);

        visitedPoints.add(startingPoint);
        SearchPath startingPath = new SearchPath(startingPoint);
        commitPlayerMovement(startingPath);
    }

    /**
     * returns the possible area in which the treasure could be
     *
     * @return
     */
    public Geometry getPossibleArea() {
        return this.possibleArea.getObject();
    }

    /**
     * Extends the bounding Area when the player comes close to its edge or the player has exited the bounding Area
     * At later stages the circle could be extended to a lineString, which only extends around the player, not the starting point
     */
    private void adaptBoundingCircle() {

        double distToBoundary = boundingCircleSize - currentPlayersPosition.distance(startingPoint);
        if (extensions < maxExtensions) {
            while ((distToBoundary < circleExtensionDistance || !boundingCircle.getObject().inside(currentPlayersPosition.getCoordinate())) && (extensions < maxExtensions)) {

                boundingCircleSize += boundingCircleExtensionDelta;
                log.info("extending Bounding Area by " + boundingCircleSize + "to " + boundingCircleSize);
                boundingCircle = new GeometryItem<>(new Circle(startingPoint.getCoordinate(), boundingCircleSize), GeometryType.BOUNDING_CIRCE, boundingCircleStyle);
                possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{boundingCircle.getObject().toPolygon()}, geometryFactory), GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);

                //now recompute all the intersections of Hints and the Bounding Circle
                for (AngleHint hint : givenHints) {

                    commitHint(hint);
                }
                extensions++;
                distToBoundary = boundingCircleSize - currentPlayersPosition.distance(startingPoint);
            }
        }
    }

    /**
     * Updates the GameField's state with a new Players SearchPath
     * <p>
     * this method assumes that the the searcher cannot fly, e.g. For 2 visited points,
     * all points on the line between them are counted as checked,
     * and of those point, all points within a distance of searcherScoutRadius are checked as well
     *
     * @param searchPath the new SearchPath
     */
    public void commitPlayerMovement(SearchPath searchPath) {
        currentPlayersPosition = searchPath.getLastPoint();
        adaptBoundingCircle();

        List<Point> newMovementPoints = searchPath.getPoints();

        log.trace("supplied searchpath " + newMovementPoints);
        this.visitedPoints.addAll(newMovementPoints);

        log.trace("total Walked Path " + visitedPoints.toString());
        this.walkedPathLength += searchPath.getLength(null);
        log.trace("total pathlength");
        Polygon checkedPoly;
        if (visitedPoints.size() > 1) {

            Coordinate[] visitedCoords = visitedPoints.stream().map(p -> p.getCoordinate()).toArray(Coordinate[]::new);
            LineString walkedPath = geometryFactory.createLineString(visitedCoords);
            checkedPoly = (Polygon) walkedPath.buffer(searcherScoutRadius);
            this.walkedPathLength = walkedPath.getLength();
        } else {
            log.trace("1 point visited so far");
            checkedPoly = (Polygon) startingPoint.buffer(searcherScoutRadius);
        }

        Geometry possible = possibleArea.getObject().difference(checkedPoly);
        checkedArea = new GeometryItem<>(checkedPoly, GeometryType.NO_TREASURE, checkedAreaStyle);
        possibleArea = new GeometryItem<>(possible, GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);

    }

    /**
     * Integrates the 2 intersections between the bounding circle and the current hint,
     * then merges the resulting polygon and the remaining possible area
     * to the new possible area.
     * <p>
     * The resulting area is NOT committed, therefore this method can be used to test a possibleHint for its result
     *
     * @param hint The hint to integrate
     * @return the resulting Geometry
     **/
    public Geometry testHint(AngleHint hint) {
        if (treasureMovedthisTurn) {
            hint.addAdditionalItem(innerBufferItem);
        }

        GeometryAngle angle = hint.getGeometryAngle();

        double rightAngle = Angle.angle(angle.getCenter(), angle.getRight());
        double extend = angle.extend();
        log.trace("testing Angle of size size: " + Angle.toDegrees(extend));

        // Use Arc for Angle Representation
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(geometryFactory);
        shapeFactory.setNumPoints(4);
        shapeFactory.setCentre(angle.getCenter());
        shapeFactory.setSize(boundingCircleSize * 8);
        LineString arcLine = shapeFactory.createArc(rightAngle, extend);
        GeometryItem<LineString> angleArea = new GeometryItem<>(arcLine, GeometryType.BOUNDING_CIRCE);
        //hint.addAdditionalItem(angleArea);

        ArrayList<Coordinate> arCoords = new ArrayList<>(Arrays.asList(arcLine.getCoordinates()));

        arCoords.add(angle.getCenter());
        arCoords.add(arCoords.get(0));

        Coordinate[] arcArray = new Coordinate[arCoords.size()];
        arcArray = arCoords.toArray(arcArray);
        Polygon arc = geometryFactory.createPolygon(arcArray);

        List<Geometry> possibleAreas = new ArrayList<>();
        log.trace("number of geoms in possible " + possibleArea.getObject().getNumGeometries());
        /*
        // first take all polygons separately
        for(int geometryNumber = 0; geometryNumber < possibleArea.getObject().getNumGeometries() ; geometryNumber++){

            //Intersect with arc
            Geometry possibleAreaWithNewHint = possibleArea.getObject().getGeometryN(geometryNumber).intersection(arc);
            log.debug("number of geometries after intersecting the " + geometryNumber + "th possible Part with arc:" + possibleAreaWithNewHint.getNumGeometries());
            log.debug("hint angle" + Angle.toDegrees(hint.getGeometryAngle().getNormalizedAngle()));

            // again multiple polygons
            List<Geometry> differenceGeometries = new ArrayList<>();

            // take the visited part out for each one
            Geometry resultingDifferenceGeometry = possibleAreaWithNewHint.getGeometryN(0).difference(this.checkedArea.getObject());

            for(int geometryNumber2 = 1 ; geometryNumber2 < possibleAreaWithNewHint.getNumGeometries(); geometryNumber2++){
                Geometry differenceOperationGeometry = possibleAreaWithNewHint.getGeometryN(geometryNumber2).difference(this.checkedArea.getObject());
                differenceGeometries.add(differenceOperationGeometry);

                //and union them again
                resultingDifferenceGeometry = resultingDifferenceGeometry.union(differenceOperationGeometry);

            }

            possibleAreas.add(resultingDifferenceGeometry);
        }

        // now union them to one resulting Geometry
        Geometry resultingPossibleArea = possibleAreas.get(0);
        for(int geometryNumber = 1; geometryNumber < possibleAreas.size(); geometryNumber++){
            resultingPossibleArea = resultingPossibleArea.union(possibleAreas.get(geometryNumber));
        }
        */

        Geometry resultingPossibleArea = possibleArea.getObject().intersection(arc);

        GeometryItem<Geometry> circleIntersection = new GeometryItem<>(arc, GeometryType.OUTER_CIRCLE, new GeometryStyle(true, new Color(0x800080)));

        // now fill Hint with HelperStructs
        hint.addAdditionalItem(circleIntersection);
        hint.addAdditionalItem(checkedArea);
        hint.addAdditionalItem(new GeometryItem<>(resultingPossibleArea, GeometryType.POSSIBLE_TREASURE, possibleAreaStyle));
        hint.addAdditionalItem(boundingCircle);

        return resultingPossibleArea;
    }

    /**
     * Finally commits the hint and integrates it with the current possibleArea
     *
     * @param hint the Hint to be integrated
     * @return the resulting Area in which the Treasure could be
     */
    public Geometry commitHint(AngleHint hint) {
        Geometry newPossibleArea = testHint(hint);
        this.possibleArea = new GeometryItem<>(newPossibleArea, GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);
        if (!givenHints.contains(hint)) {
            givenHints.add(hint);
        }
        treasureMovedthisTurn = false;
        return newPossibleArea;
    }

    /**
     * Returns whether the specified point lies within the possible Area,
     * treasure could live in
     *
     * @param p
     * @return
     */
    public boolean isWithinGameField(Point p) {
        return boundingCircle.getObject().inside(p.getCoordinate());
    }


    /**
     * Checks all edges of the possibleArea for the Coordinate which maximizes the value of {dist(C - Player) / dist( C -Origin) }
     *
     * @return a List of {@link org.locationtech.jts.geom.Coordinate} with their corresponding Constant of (Coordinates C ; their associated Value of {dist(C-Player)/dist(C-Origin)} )
     */
    public List<Pair<Coordinate, Double>> getWorstPointsOnAllEdges(double minDistance) {

        // making the decision transparent by drawing all lines which are inspected
        Geometry innerBufferedArea = possibleArea.getObject().buffer(-0.001);

        // due to rounding errors in the calculation a minimally negative buffer is to be drawn
        //innerBufferItem = new GeometryItem(innerBufferedArea.copy(), GeometryType.INNER_BUFFER, innerBufferStyle);

        Geometry geometryToCheck;
        if (minDistance > this.searcherScoutRadius) {
            Circle minDistanceCircle = new Circle(startingPoint.getCoordinate(), minDistance);
            geometryToCheck = innerBufferedArea.difference(minDistanceCircle.toPolygon());
        } else { //dont bother
            geometryToCheck = innerBufferedArea;
        }

        innerBufferItem = new GeometryItem(geometryToCheck.copy(), GeometryType.INNER_BUFFER, innerBufferStyle);

        log.trace("# geometries " + this.possibleArea.getObject().getNumGeometries());
        log.trace("# geometries buffered " + this.possibleArea.getObject().buffer(-0.1).getNumGeometries());

        Pair<Coordinate, Double> bestSampled = new Pair(this.favoredTreasureLocation.getObject().getCoordinate(), (this.favoredTreasureLocation.getObject().getCoordinate().distance(this.currentPlayersPosition.getCoordinate()) + this.walkedPathLength) / this.favoredTreasureLocation.getObject().getCoordinate().distance(this.startingPoint.getCoordinate()));
        Pair<Coordinate, Double> bestCalc = new Pair(this.favoredTreasureLocation.getObject().getCoordinate(), this.favoredTreasureLocation.getObject().getCoordinate().distance(this.currentPlayersPosition.getCoordinate()) / this.favoredTreasureLocation.getObject().getCoordinate().distance(this.startingPoint.getCoordinate()));

        // always consider just leaving the treasure as is
        List<Pair<Coordinate, Double>> worstEdgePoints = new ArrayList<>();
        worstEdgePoints.add(bestCalc);
        List<Pair<Coordinate, Double>> worstEdgePointsSampled = new ArrayList<>();
        worstEdgePointsSampled.add(bestSampled);

        // for the case, that the player's visited area divides the possible area into 2 or more Polygons, a seperate search for each geometry is required
        for (int geomNumber = 0; geomNumber < geometryToCheck.getNumGeometries(); geomNumber++) {
            log.trace(" geom " + geomNumber + " has " + geometryToCheck.getGeometryN(geomNumber).getCoordinates().length + " coords");
            Geometry currentGeometry = geometryToCheck.getGeometryN(geomNumber);
            Coordinate[] edges = currentGeometry.getCoordinates();

            log.trace("Sampling " + edges.length + " edges in run " + visitedPoints.size());

            Pair<Coordinate, Double> sampledOnEdge;
            Pair<Coordinate, Double> calcOnEdge;

            // now iterate over the Coordinates of the current Geometry
            for (int currentCoordinateIndex = 1; currentCoordinateIndex < edges.length; currentCoordinateIndex++) {
                calcOnEdge = calcMaxConstantPointOnSegment(edges[currentCoordinateIndex - 1], edges[currentCoordinateIndex], this.currentPlayersPosition.getCoordinate());
                worstEdgePoints.add(calcOnEdge);
                sampledOnEdge = sampleMaxConstantWithPathOnLineSegment(edges[currentCoordinateIndex - 1], edges[currentCoordinateIndex], this.currentPlayersPosition.getCoordinate());
                worstEdgePointsSampled.add(sampledOnEdge);
                log.trace("line (" + edges[currentCoordinateIndex - 1] + ", " + edges[currentCoordinateIndex] + ")");
                log.trace("Calc " + calcOnEdge);
                log.trace("Samp " + sampledOnEdge);
                if (sampledOnEdge.getValue() > bestSampled.getValue()) {
                    bestSampled = sampledOnEdge;
                }
                if (calcOnEdge.getValue() > bestCalc.getValue()) {
                    bestCalc = calcOnEdge;
                    log.trace("new best Const " + bestCalc.getValue() + " at " + bestCalc.getKey() + " at line (" + edges[currentCoordinateIndex - 1] + ", " + edges[currentCoordinateIndex] + ")");
                }
            }
        }

        //sort for constant
        worstEdgePoints.sort(new Comparator<Pair<Coordinate, Double>>() {
            @Override
            public int compare(Pair<Coordinate, Double> coordinateDoublePair, Pair<Coordinate, Double> t1) {
                return coordinateDoublePair.getValue().compareTo(t1.getValue());
            }
        }.reversed());

        worstEdgePointsSampled.sort(new Comparator<Pair<Coordinate, Double>>() {
            @Override
            public int compare(Pair<Coordinate, Double> coordinateDoublePair, Pair<Coordinate, Double> t1) {
                return coordinateDoublePair.getValue().compareTo(t1.getValue());
            }
        }.reversed());

        log.trace("worst Point List:" + worstEdgePoints.size() + "first element: " + worstEdgePoints.get(0));
        log.debug("best overall Sampled constant is" + bestSampled.getValue() + " at " + bestSampled.getKey().toString());
        log.debug("best overall calculated constant is" + bestCalc.getValue() + " at " + bestCalc.getKey().toString());
        if (PreferenceService.getInstance().getPreference(MobileTreasureHider.walkedPathLengthForTreasureRelocation_Preference, 1).intValue() == 1) {
            return worstEdgePointsSampled;
        } else {
            return worstEdgePoints;
        }
    }


    /**
     * Todo
     *
     * @param geometry the geometry whose LineSegment are educed to its best Point according to the worstConstant - formula
     * @return a List of {@link org.locationtech.jts.geom.Coordinate} with their corresponding Constant of (Coordinates C ; their associated Value of {dist(C-Player)/dist(C-Origin)} )
     */
    public List<Pair<Coordinate, Double>> getWorstPointOnGeometry(LineString geometry) {
        return null;
    }

    /**
     * Alternate to {@link this.sampleMaxConstantPointOnLineSegment}
     * Calculates the Point on the boundarySegment described by p1,p2 of the possible Area which results in the largest Constant of PathMin * C = PathActual
     *
     * @param p1     the Start of the Segment v
     * @param p2     the End of the Segment v
     * @param player current Position of the Player
     * @return the Point and its corresponding constant C
     */
    private Pair<Coordinate, Double> calcMaxConstantPointOnSegment(Coordinate p1, Coordinate p2, Coordinate player) {
        LineSegment unscaled = new LineSegment(p1, p2);

        Point origin = startingPoint;
        assert origin.equals(geometryFactory.createPoint(new Coordinate(0, 0))); // origin assumed to be (0,0)

        Coordinate projection = unscaled.project(origin.getCoordinate());
        double scalingFactor = 1.0 / projection.distance(origin.getCoordinate());

        AffineTransformation scalingTransform = AffineTransformation.scaleInstance(scalingFactor, scalingFactor);

        //determine rotation angle to put ProjectionPoint onto the Y-Axis
        double rotationAngle = Angle.angleBetweenOriented(projection, origin.getCoordinate(), new Coordinate(0, 1));

        AffineTransformation rotationTransform = AffineTransformation.rotationInstance(rotationAngle);

        Coordinate p1Transformed = p1.copy();
        Coordinate p2Transformed = p2.copy();
        Coordinate playerTransformed = player.copy();

        log.trace("scaling by Factor" + scalingFactor);
        p1Transformed = scalingTransform.transform(p1Transformed, p1Transformed);
        p2Transformed = scalingTransform.transform(p2Transformed, p2Transformed);
        playerTransformed = scalingTransform.transform(playerTransformed, playerTransformed);
        log.trace("after scaling: " + new LineSegment(p1Transformed, p2Transformed));
        log.trace(playerTransformed.toString());

        log.trace("rotation by Angle" + Angle.toDegrees(rotationAngle));
        p1Transformed = rotationTransform.transform(p1Transformed, p1Transformed);
        p2Transformed = rotationTransform.transform(p2Transformed, p2Transformed);
        playerTransformed = rotationTransform.transform(playerTransformed, playerTransformed);

        LineSegment normalizedLineSegment = new LineSegment(p1Transformed, p2Transformed);

        //now normal state is reached and Derivative can be applied for MaxConstant Search

        log.trace("transformed player is now on " + playerTransformed);
        double a = playerTransformed.x;
        double b = playerTransformed.y;
        double maximum, extremum1, extremum2, variableTerm;

        if (a != 0.0) { //player is not on Y-Axis --> 2 Extrema
            log.trace(" player NOT on Y-Axis");
            variableTerm = Math.sqrt(Math.pow(-2 * Math.pow(a, 2.0) - 2 * Math.pow(b, 2.0) + 4 * b, 2.0) + 16 * Math.pow(a, 2.0));

            extremum1 = (variableTerm + 2 * Math.pow(a, 2.0) + 2 * Math.pow(b, 2.0) - 4 * b) / (4 * a);
            extremum2 = (-variableTerm + 2 * Math.pow(a, 2.0) + 2 * Math.pow(b, 2.0) - 4 * b) / (4 * a);
            log.trace(" extremum1 : " + extremum1);
            log.trace(" extremum2 : " + extremum2);
            //now plug into 2nd Derivative to see who is Max and whos Min
            if (evalSecondDerivativeOfConstFunction(extremum1, a, b) < 0) {
                log.trace("Extremum 1 is a maxima");
                maximum = extremum1;
            } else if (evalSecondDerivativeOfConstFunction(extremum2, a, b) < 0) {
                log.trace("Extremum 2 is a maxima");
                maximum = extremum2;
            } else {
                log.trace("This should not be possible");
                maximum = 0.0;
            }
            //SafetyCheck necessary here, but long formula .....

        } else { // either both points are Identical( x can be chosen arbitrarily), or x == 0
            log.trace(" player on Y-Axis");
            if (b == 0.0 || b == 2.0) { // all points on line have the same constant ==> choose x=0
                maximum = 0;
            } else if (b > 0 || b > 2) { // 0 maximizes the Constant
                maximum = 0;

            } else { //  0 < b < 2 --> 0 minimizes the Constant --> choose the Endpoint of the segment, which is further away
                maximum = Math.abs(p1Transformed.x) > Math.abs(p2Transformed.x) ? p1Transformed.x : p2Transformed.x;
            }
        }
        log.trace("Calculated Maximum on Line is Point : " + maximum + " , 1");

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
        log.trace("Calculated Maximum on LineSEGMENT is Point : " + maximum + " , 1");
        Coordinate bestCoordinate = new Coordinate(maximum, 1.0);
        // Now Transform that Point back and/or Calc the Angle which it has
        try {
            bestCoordinate = rotationTransform.getInverse().transform(bestCoordinate, bestCoordinate);
            bestCoordinate = scalingTransform.getInverse().transform(bestCoordinate, bestCoordinate);

            //bit hacky but needed to make bestCoordinate lie on the LineString
            log.trace("Distance from " + bestCoordinate + "to " + unscaled + " : " + unscaled.distance(bestCoordinate)); // are in the range of 10e-16
            bestCoordinate = unscaled.project(bestCoordinate);
            log.trace("after projection; Distance from " + bestCoordinate + "to " + unscaled + " : " + unscaled.distance(bestCoordinate)); // stays in the 10e-16 range

        } catch (NoninvertibleTransformationException e) {
            log.error("Non invertible, but it should be!", e);

        }
        log.trace("Final Point after InverseTransformation: " + bestCoordinate);
        log.trace("player pos:" + player);
        log.trace("origin pos " + origin.getCoordinate());
        return new Pair<>(bestCoordinate, evaluateConstantFunction(player, bestCoordinate, origin.getCoordinate()));

    }


    private double evaluateConstantFunction(Coordinate player, Coordinate pointToEvaluate, Coordinate origin) {
        return player.distance(pointToEvaluate) / origin.distance(pointToEvaluate);
    }


    private double evalSecondDerivativeOfConstFunction(double x, double a, double b) {
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
        log.trace("checking Linesegment(" + p1.getX() + ", " + p1.getY() + ") -> (" + p2.getX() + ", " + p2.getY() + ")");
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
                log.trace("new Best Point found on Linesegment at " + step + "/" + samples + "with C=" + constant);
            }
        }
        return bestPoint;
    }

    private Pair<Coordinate, Double> sampleMaxConstantWithPathOnLineSegment(Coordinate p1, Coordinate p2, Coordinate player) {
        log.trace("checking Linesegment(" + p1.getX() + ", " + p1.getY() + ") -> (" + p2.getX() + ", " + p2.getY() + ")");
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
            double constant = (distToPlayer + this.walkedPathLength) / distToStart;
            if (constant > bestPoint.getValue()) {
                bestPoint = new Pair<>(currentPoint, constant);
                log.trace("new Best Point found on Linesegment at " + step + "/" + samples + "with C=" + constant);
            }
        }
        return bestPoint;
    }

}
