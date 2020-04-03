package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.*;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An abstract hider which implements methods to play a basic game
 * It also supplies methods to judge a given hint by generating statistics about the hint and saving them in {@link StatisticalHider}
 * <p>
 * How these hints are rated is up to the {@link StatisticalHider} subclasses by implementing the {@link #rateHint(AngleHintStatistic)} method
 */
@Slf4j
public abstract class StatisticalHider {

    public static final String getRelativeAreaCutoffWeight_Preference = "relative area cutoff weight";
    public static final String DistanceFromNormalAngleLineToTreasureWeight_Preference = "distance to angle bisector line weight";
    public static final String DistanceFromResultingCentroidToTreasureWeight_Preference = "distance to centroid weight";


    protected GameField gameField;
    protected GeometryFactory gf = JTSUtils.GEOMETRY_FACTORY;

    protected Point startingPoint;
    protected Geometry currentPossibleArea;

    //possible Measurements
    protected double centroidDistanceToTreasure;
    protected double absoluteAreaCutoff;
    protected double relativeAreaCutoff;
    protected double counterStrategyGeometryCutoff;
    protected double distanceFromNormalAngleRay;


    protected Point treasure;
    protected double preferredHintSize;

    public void init(Point searcherStartPosition) {
        log.info("StatisticalHider init");
        this.gameField = new GameField();
        startingPoint = searcherStartPosition;

        PreferenceService pS = PreferenceService.getInstance();
        treasure = JTSUtils.shuffleTreasure();
        preferredHintSize = pS.getPreference(PreferenceService.HintSize_Preference, 180).doubleValue();

        gameField.init(searcherStartPosition, treasure);
        this.currentPossibleArea = gameField.getPossibleArea();
    }

    public AngleHint move(SearchPath searchPath) {

        // update GameField
        gameField.commitPlayerMovement(searchPath);

        // generate Hints
        List<AngleHint> possibleHints = generateHints(360, searchPath.getLastPoint() );

        // evaluateHints --> use the GameField
        AngleHint hint = eval(possibleHints);

        // commitHint
        gameField.commitHint(hint);
        this.currentPossibleArea = gameField.getPossibleArea();
        // return Hint
        return hint;
    }

    /**
     * Evaluates the Hints by generating some statistics on them,
     * rating the Hints, and returning the best.
     * @param hints the AngleHints to evaluate
     * @return the chosen AngleHint
     */
    public AngleHint eval(List<AngleHint> hints) {

        List<AngleHintStatistic> stats = new ArrayList<>();
        Geometry before = this.currentPossibleArea;
        log.info("current possibleArea size " + before.getArea());
        for(AngleHint hint: hints){

            Geometry after = gameField.testHint(hint);
            AngleHintStatistic hs = new AngleHintStatistic(hint,before,after); // autofills the absolute/relative area cutoffs

            //calc some Statistics
            fillDistanceToNormalLine(hs);
            fillDistanceToCentroid(hs);

            stats.add(hs);
            rateHint(hs);
        }
        log.trace("#of hints" + stats.size());
        stats = filterForValidHints(stats);
        log.trace("#of hints after filtering for InView Predicate" + stats.size());

        stats.sort(Comparator.comparingDouble(AngleHintStatistic::getRating).reversed());

        AngleHintStatistic returnHint = stats.get(0);

        log.info("angleHint with the best rating");
        log.info(returnHint.toString());

        //add some status information
        String possibleAreaPretty = new DecimalFormat("#.00").format(returnHint.getAreaAfterHint().getArea());
        returnHint.getHint().getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.REMAINING_POSSIBLE_AREA, possibleAreaPretty ));

        String relAeaCutoffPretty = new DecimalFormat("#.00").format(returnHint.getRelativeAreaCutoff() * 100) + "%";
        returnHint.getHint().getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.RELATIVE_AREA_CUTOFF, relAeaCutoffPretty));

        String cDistPretty = new DecimalFormat("#.00").format(returnHint.getDistanceFromResultingCentroidToTreasure());
        returnHint.getHint().getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.DISTANCE_TREASURE_TO_CENTROID, cDistPretty));

        String bisectorDistPretty = new DecimalFormat("#.00").format(returnHint.getDistanceFromNormalAngleLineToTreasure());
        returnHint.getHint().getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.DISTANCE_ANGLE_BISECTOR_TREASURE, bisectorDistPretty ));

        return returnHint.getHint();
    }

    /**
     * Rates the Given Hint with a custom function, saves the result in the AngleHintStatistic - Wrapper and returns the result
     * @param ahs The AngleHint - Wrapper , in which the result is saved
     * @return the rating of the hint
     */
    protected abstract double rateHint(AngleHintStatistic ahs);

    /**
     * Inspects the given Hint in AngleHintStatistic and computes the distance between the angle-bisector and the treasure.
     * The result is written into the corresponding AngleHintStatistic
     *
     * @param ahs the AngleHintStatistic Wrapper containing the Hint
     * @return the distance
     */
    protected double fillDistanceToNormalLine(AngleHintStatistic ahs){
        Coordinate center = ahs.getHint().getGeometryAngle().getCenter();
        Coordinate middlePoint = JTSUtils.middleOfAngleHint(ahs.getHint());
        LineSegment normalLine = new LineSegment(center,middlePoint);

        double distToNormal = normalLine.project(treasure.getCoordinate()).distance(treasure.getCoordinate());
        ahs.setDistanceFromNormalAngleLineToTreasure(distToNormal);
        return distToNormal;
    }

    /**
     * Inspects the given Hint in AngleHintStatistic and computes the distance between the angle-bisector and the treasure.
     * The result is written into the corresponding AngleHintStatistic
     *
     * @param ahs the AngleHintStatistic Wrapper containing the Hint
     * @return the distance
     */
    protected double fillDistanceToCentroid(AngleHintStatistic ahs){
        Geometry after = ahs.getAreaAfterHint();
        Point centroid = after.getCentroid();

        GeometryStyle centroidStyle = new GeometryStyle(true, new Color(244, 149, 41));
        GeometryItem<Point> centroidItem = new GeometryItem(centroid, GeometryType.CENTROID, centroidStyle);
        ahs.getHint().addAdditionalItem(centroidItem);

        double dist = centroid.distance(treasure);
        ahs.setDistanceFromResultingCentroidToTreasure(dist);
        return dist;
    }

    /**
     * Filters the given Hints on the Predicate of being abel to see
     *
     * @param stats the List of AngleHintStats to filter
     * @return
     */
    protected List<AngleHintStatistic> filterForValidHints(List<AngleHintStatistic> stats) {

        stats = stats.stream().filter(hint -> hint.getHint().getGeometryAngle().inView(treasure.getCoordinate())).collect(Collectors.toList());
        log.debug("remaining possible Hints" + stats.size()); // hint: its always half the amount
        return stats;
    }

    /** Todo: if hint is 180degree make it an explicit HalfPlaneHint/ hack it such that StrategyFromPaper receives a halfplaneHint
     * Generates {@code samples } evenly spaced {@link com.treasure.hunt.strategy.hint.impl} .
     * Generated hints are not always exactly the preferred hint Size, the diverge about 3 * e-13
     *
     * @param samples determines how many Hints are returned
     * @return a List of {@code samples} {@link com.treasure.hunt.strategy.hint.impl.AngleHint}
     */
    protected List<AngleHint> generateHints(int samples, Point hintCenter) {
        final double twoPi = Math.PI * 2;

        AngleHint hint;
        List<AngleHint> hints = new ArrayList<>();

        // if player out of bounding area use the line orthogonal to the line from player to boundingArea Center
        if (!gameField.isWithinGameField(hintCenter)) {
            log.info("player not in bounding circle, giving generic hint");
            // use translation then rotation on Player Point by 90degree then translate back
            //AffineTransform orthAroundPlayer = new AffineTransform(0.0, -1.0 , 2 * hintCenter.getX() , 1.0 , 0.0, -hintCenter.getX() + hintCenter.getY());
            AffineTransformation orthAroundPlayer = new AffineTransformation();
            orthAroundPlayer.rotate(Math.toRadians(90), hintCenter.getX(), hintCenter.getY());
            Coordinate orth = startingPoint.getCoordinate().copy(); //line to middle of circle
            orthAroundPlayer.transform(orth, orth);

            Point right = gf.createPoint(new Coordinate(hintCenter.getX() - (orth.x - hintCenter.getX()), hintCenter.getY() - (orth.y - hintCenter.getY())));
            Point left = gf.createPoint(orth);

            AngleHint outOfBoundsHint = new AngleHint(right.getCoordinate(), hintCenter.getCoordinate(), left.getCoordinate());
            hints.add(outOfBoundsHint);


        } else {
            log.info("generating " + samples + " hints of size " +  PreferenceService.getInstance().getPreference(PreferenceService.HintSize_Preference, 180).doubleValue());

            for (int i = 0; i < samples; i++) {
                double rightAngle = twoPi * (((double) i) / samples);
                double leftAngle = rightAngle + Angle.toRadians(preferredHintSize);

                double dX_right = Math.cos(rightAngle);
                double dY_right = Math.sin(rightAngle);
                double dX_left = Math.cos(leftAngle);
                double dY_left = Math.sin(leftAngle);
                Point right = gf.createPoint(new Coordinate(hintCenter.getX() + dX_right, hintCenter.getY() + dY_right));
                Point left = gf.createPoint(new Coordinate(hintCenter.getX() + dX_left, hintCenter.getY() + dY_left));

                //check for possible HalfPlaneHint
                if (preferredHintSize == 180) {
                    hint = new HalfPlaneHint(hintCenter.getCoordinate(), right.getCoordinate());
                } else {
                    hint = new AngleHint(right.getCoordinate(), hintCenter.getCoordinate(), left.getCoordinate());
                }

                hints.add(hint);
            }
        }
        return hints;
    }

    public Point getTreasureLocation(){
        return this.treasure;
    }

}
