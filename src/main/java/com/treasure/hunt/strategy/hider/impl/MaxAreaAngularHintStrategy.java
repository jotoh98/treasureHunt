package com.treasure.hunt.strategy.hider.impl;


import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.jts.geom.Line;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    private List<AngleHint> givenHints;
    private GeometryFactory gf;
    private double walkedPathLength = 0.0;
    private List<Point> visitedPoints = new ArrayList<>();

    @Getter
    private GeometryItem<Geometry> possibleArea;
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
    public void init(Point searcherStartPosition){
        log.info("MaxAreaAngularHintStrategy init");
        gf = JTSUtils.GEOMETRY_FACTORY;
        givenHints = new ArrayList<>();
        startingPoint = searcherStartPosition;
        visitedPoints.add(startingPoint);


        currentPlayersPosition = startingPoint;
        Circle c = new Circle(startingPoint.getCoordinate(), boundingCircleSize, gf);
        boundingCircle = new GeometryItem<>(c, GeometryType.BOUNDING_CIRCE);

        possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{c}, gf), GeometryType.POSSIBLE_TREASURE);
        this.pointWithWorstConstant = new GeometryItem<>(gf.createPoint(new Coordinate(100.0,0)), GeometryType.WORST_CONSTANT);
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

        Geometry newPossibleArea = possibleArea.getObject().intersection(hintedArea).difference(checkedArea.getObject());

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
5     * @return
     */
    private AngleHint generateHint(int samples, Point origin) {
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


        this.pointWithWorstConstant = new GeometryItem<>(gf.createPoint(getMaxConstantPoint()), GeometryType.WORST_CONSTANT);

        //this.pointWithWorstConstant = new GeometryItem<>(gf.createPoint(new Coordinate(20,-50)),GeometryType.WORST_CONSTANT);
        log.info("results: (" + this.pointWithWorstConstant.getObject().getX() + ", " + this.pointWithWorstConstant.getObject().getY() + ")");

        for (double i = 0; i < samples; i++) {
            double angle = twoPi * (i / samples);
            dX = Math.cos(angle);
            dY = Math.sin(angle);
            right = gf.createPoint(new Coordinate(origin.getX() + dX, origin.getY() + dY));
            left = gf.createPoint(new Coordinate(origin.getX() - dX, origin.getY() - dY));


            hint = new AngleHint(right.getCoordinate(), origin.getCoordinate(), left.getCoordinate());
            resultingGeom = integrateHint(hint);

            features[(int) i][0] = resultingGeom.getArea();

            // get the largest area, but only if the Geometry contains the Point with the best constant
            if (features[(int) i][0] > maxArea) {

                log.info("Containment Test");
                log.info(" contains: " + resultingGeom.contains(pointWithWorstConstant.getObject()));
                log.info(" covers: " +resultingGeom.covers(pointWithWorstConstant.getObject()));
                log.info(" buffer contains: " +resultingGeom.buffer(0.0001).contains(pointWithWorstConstant.getObject()));
                log.info(" buffer covers: " +resultingGeom.buffer(0.0001).covers(pointWithWorstConstant.getObject()));

                if (resultingGeom.distance(pointWithWorstConstant.getObject()) < 0.00001){//resultingGeom.buffer(0.0001).contains(this.pointWithWorstConstant.getObject())) {

                    log.info("area contains Worst Constant Point --> viable hint");
                    //log.debug("containment: " + resultingGeom.contains(this.pointWithWorstConstant.getObject()) + ", coverage: " + resultingGeom.covers(gf.createPoint(new Coordinate(99.87961816680493, 2.450428508239015))));
                    maxGeometry = resultingGeom;
                    maxArea = features[(int) i][0];
                    maxAngle = hint;
                    log.info("current max angle hint with Containtment of Worst Point: " + hint.getGeometryAngle().getRight() + " , " + hint.getGeometryAngle().getCenter() + " " + hint.getGeometryAngle().getLeft());
                }


            }
        }
        Coordinate[] pl= new Coordinate[5];
        pl[0] = new Coordinate(1.0,1.0);
        pl[1] = new Coordinate(-1.0,1.0);
        pl[2] = new Coordinate(-1.0,-1.0);
        pl[3] = new Coordinate(1.0,-1.0);
        pl[4] = new Coordinate(1.0,1.0);

        Geometry tPoly = gf.createPolygon(pl);
        log.info("Custom Poly: " + tPoly.covers(gf.createPoint(new Coordinate(0.0,0.0))));
        log.info("Custom Poly2: " + tPoly.covers(gf.createPoint(new Coordinate(1.0,0.0))));
        log.info("Custom Poly3: " + tPoly.covers(gf.createPoint(new Coordinate(1.0,0.5))));
        assert maxAngle != null;
        assert maxGeometry != null : "MaxGeometry is NULL!";
        log.info(" the maxGeometry " +  maxGeometry);
        log.info(" the worst Point " + this.pointWithWorstConstant.getObject());
        log.info(" the maxGeometry covers: " + maxGeometry.covers(pointWithWorstConstant.getObject()));
        this.possibleArea = new GeometryItem<>(maxGeometry, GeometryType.POSSIBLE_TREASURE, new GeometryStyle(true, new Color(255,105,180)));
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
        log.info("Sampling " +  edges.length + " edges in run " + visitedPoints.size());
        Pair<Coordinate, Double> bestPoint = new Pair(edges[0], edges[0].distance(this.currentPlayersPosition.getCoordinate()) / edges[0].distance(this.startingPoint.getCoordinate()));
        Pair<Coordinate, Double> bestPointOnEdge;

        for (int currentCoordinateIndex = 1; currentCoordinateIndex < edges.length; currentCoordinateIndex++) {
            //calculatedPoint = calcMaxConstantPointOnSegment(edges[currentCoordinateIndex - 1], edges[currentCoordinateIndex], this.currentPlayersPosition.getCoordinate());
            bestPointOnEdge = sampleMaxConstantPointOnLineSegment(edges[currentCoordinateIndex - 1], edges[currentCoordinateIndex], this.currentPlayersPosition.getCoordinate());
            LineSegment tester = new LineSegment(edges[currentCoordinateIndex - 1], edges[currentCoordinateIndex]);
            log.info( "Containment test on LS + " + tester.toString() + " : " + tester.distance(bestPointOnEdge.getKey()) );
            Coordinate[] pl= new Coordinate[5];
            pl[0] = edges[currentCoordinateIndex - 1];
            pl[1] = edges[currentCoordinateIndex];
            pl[3] = new Coordinate(-1.0,1.0);
            pl[2] = new Coordinate(-1.0,-1.0);
            pl[4] = edges[currentCoordinateIndex - 1];

            Geometry x = gf.createPolygon(pl);

            if (bestPointOnEdge.getValue() > bestPoint.getValue()) {
                bestPoint = bestPointOnEdge;
            }
        }
        //calcMaxConstantPointOnSegment(new Coordinate(8.0,4.0) , new Coordinate(8.0,-4.0), this.currentPlayersPosition.getCoordinate());
        //calculatedPoint = calcMaxConstantPointOnSegment(edges[edges.length - 1], edges[0], this.currentPlayersPosition.getCoordinate());

        log.info("best OVERALL constant is" + bestPoint.getValue() + " at " + bestPoint.getKey().toString());
        return bestPoint.getKey();
    }

    /** WIP parameterized Constant works, but no Extremepoint Search yet
     *  Alternate to {@sampleMaxConstantPointOnLineSegment}
     * Calculates the Point on the boundarySegment described by p1,p2 of the possible Area which results in the largest Constant of PathMin * C = PathActual
     *
     * @param p1 the Start of the Segment v
     * @param p2 the End of the Segment v
     * @param player current Position of the Player
     * @return the Point and its corresponding constant C
     */
    private Pair<Coordinate,Double> calcMaxConstantPointOnSegment(Coordinate p1, Coordinate p2, Coordinate player){
        log.info("----- Calc of Constant begun -----");
        log.info("Point P1: (" + p1.x + ", " + p1.y + ") Point P2: (" + p2.x + ", " + p2.y +")" );

        Coordinate origin = this.startingPoint.getCoordinate();
        //first get all the constants
        double betweenp1p2 = Angle.angleBetweenOriented(p1,player,p2);
        Coordinate a;
        Coordinate b;
        double betweenAandB;
        if(betweenp1p2 < 0){ // Let a be the "left" Point
            a = p1;
            b = p2;
            betweenAandB = betweenp1p2 * -1;
        }else{
            b = p1;
            a = p2;
            betweenAandB = betweenp1p2;
        }
        double alpha = Angle.toRadians(56.4079729245763); //dummy
        alpha = Angle.toRadians(23.9624889745782); //testangle
        log.info("Point A: (" + a.x + ", " + a.y + ") Point B: (" + b.x + ", " + b.y +") Point Player: (" + player.x + ", " + player.y +") Point Origin: (" + origin.x + ", " + origin.y +")" );

        log.info("Angle between A and B: " + Angle.toDegrees(betweenAandB) + " degrees");
        Coordinate xParrallelTip = new Coordinate(a.x +1 , a.y);
        double theta = Angle.angleBetweenOriented(player,a,xParrallelTip);
        log.info("Angle Theta between Player,A and xAxis: " + Angle.toDegrees(theta) + " degrees");
        double beta = Angle.angleBetweenOriented(player,a,b);

        log.info("Angle Beta between o and a: " + Angle.toDegrees(beta) + " degrees");

        double length_o = player.distance(a);
        double length_l = length_o * (Math.sin(beta) / (Math.sin(Angle.toRadians(180) - alpha - beta)));

        log.info("Calculated Distance of lineSegment l from Point Player to Point S of angle Alpha=" + Angle.toDegrees(alpha) +": " + length_l);

        double length_q = origin.distance(player);
        double tau = Angle.angleBetweenOriented(origin,player,a);
        log.info("Angle Tau between q and o: " + Angle.toDegrees(tau) + " degrees");

        double eta = tau - alpha;
        double length_r = Math.sqrt(Math.pow(length_q,2.0) + Math.pow(length_l,2.0) - 2.0* length_q * length_l * Math.cos(eta));
        log.info("Calculated Distance of lineSegment r from Point O to Point S of angle Alpha=" + Angle.toDegrees(alpha) +": " + length_r);

        Pair<Coordinate,Double> calculatedPoint = new Pair<>(a,length_l);

        log.info("----- Calc of Constant END -----");
        return calculatedPoint;

    }

    /**
     * Samples for the Point on the boundarySegment described by p1,p2 of the possible Area which results in the largest Constant of PathMin * C = PathActual
     *
     * @param p1 the Start of the Segment v
     * @param p2 the End of the Segment v
     * @param player current Position of the Player
     * @return the Point and its corresponding constant C
     */
    private Pair<Coordinate, Double> sampleMaxConstantPointOnLineSegment(Coordinate p1, Coordinate p2, Coordinate player) {
        log.info("checking Linesegment(" + p1.getX() + ", " + p1.getY() + ") -> (" + p2.getX() + ", " + p2.getY() + ")");
        double eps = 0.5; //the maximum distance between two sampled Point on the LineSegment

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
        /*
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

         */
    }

    private void integrateMovementWithCheckedArea(Movement movement){
        List<Point> newMovemenPoints = movement.getPoints().subList(1,movement.getPoints().size()).stream().map( p -> p.getObject()).collect(Collectors.toList());
        log.info("LIst size "+ newMovemenPoints.size());
        log.info("LIst "+ newMovemenPoints.toString());
        log.info("before: " + visitedPoints.toString());
        log.info(" result" + this.visitedPoints.addAll(newMovemenPoints));
        log.info("whole size "+ visitedPoints.size());
        log.info("whole view "+ visitedPoints.toString());
        Polygon checkedPoly;
        if(visitedPoints.size() > 1){
            Coordinate[] visitedCoords = visitedPoints.stream().map( p -> p.getCoordinate()).toArray(Coordinate[]::new);
            LineString walkedPath = gf.createLineString( visitedCoords);
            checkedPoly = (Polygon) walkedPath.buffer(searcherScoutRadius + 0.001); //0.001 to just be outside the searchers radius
            this.walkedPathLength = walkedPath.getLength();
        }else{
            //checkedPoly = (Polygon) newMovemenPoints.get(0).buffer(searcherScoutRadius + 0.001);
            checkedPoly = (Polygon) startingPoint.buffer(searcherScoutRadius + 0.001);
        }



        checkedArea = new GeometryItem<>(checkedPoly, GeometryType.NO_TREASURE);

    }

    @Override
    public AngleHint move(Movement movement) {
        currentPlayersPosition = movement.getEndPoint();
        adaptBoundingCircle();
        integrateMovementWithCheckedArea(movement);
        /*
        //calc the visited Area by the player TODO should be done incrementally from move to move or be globally accessible
        Coordinate[] visitedCoordinates = new Coordinate[movement.getPoints().size()];
        for (int coordinateIndex = 0; coordinateIndex < movement.getPoints().size(); coordinateIndex++) {
            visitedCoordinates[coordinateIndex] = movement.getPoints().get(coordinateIndex).getObject().getCoordinate();
        }
        LineString walkedPath = gf.createLineString(visitedCoordinates);
        this.walkedPathLength = walkedPath.getLength();
        Polygon checkedPoly = (Polygon) walkedPath.buffer(searcherScoutRadius + 0.001); //0.001 to just be outside the searchers radius
        checkedArea = new GeometryItem<>(checkedPoly, GeometryType.NO_TREASURE);
        */
        AngleHint hint = generateHint(360, currentPlayersPosition); //compute by maximizing the remaining possible Area after the Hint over 360 sample points
        givenHints.add(hint);

        hint.addAdditionalItem(checkedArea);
        hint.addAdditionalItem(possibleArea);


        hint.addAdditionalItem(boundingCircle);
        hint.addAdditionalItem(pointWithWorstConstant);

        log.info("given Hint: " + hint.getGeometryAngle().getRight() + ",  " + hint.getGeometryAngle().getCenter() + ",  " + hint.getGeometryAngle().getLeft());
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
            while((distToBoundary < circleExtensionDistance || !boundingCircle.getObject().contains(currentPlayersPosition))) {

                log.info("ext distance " + boundingCircle.getObject().isWithinDistance(currentPlayersPosition, circleExtensionDistance));
                log.info("containment " + !boundingCircle.getObject().contains(currentPlayersPosition));

                boundingCircleSize += boundingCircleExtensionDelta;
                log.info("extending Bounding Area by " + boundingCircleSize + "to " + boundingCircleSize);
                boundingCircle = new GeometryItem<>(new Circle(startingPoint.getCoordinate(), boundingCircleSize, gf), GeometryType.BOUNDING_CIRCE, new GeometryStyle(true, new Color(50, 205, 50)));
                possibleArea = new GeometryItem<>(new MultiPolygon(new Polygon[]{boundingCircle.getObject()}, gf), GeometryType.POSSIBLE_TREASURE, new GeometryStyle(true, new Color(255, 105, 180)));
                //now recompute all the intersections of Hints and the Bounding Circle
                for (AngleHint hint : givenHints) {
                    possibleArea = new GeometryItem<>(integrateHint(hint), GeometryType.POSSIBLE_TREASURE, new GeometryStyle(true, new Color(255, 105, 180)));

                }
                extensions++;
                distToBoundary = boundingCircleSize - currentPlayersPosition.distance(startingPoint);
            }
        }
    }

}
