package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.geom.*;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
public class FixedTreasureHider extends StatisticalHider implements Hider<AngleHint> {


    private Point treasure;


    public FixedTreasureHider(){
        super();
        Random rand = new Random();
        treasure = gf.createPoint(new Coordinate(Math.random() * 100,Math.random() * 100));
    }
    @Override
    public AngleHint eval(List<AngleHint> hints) {


        List<AngleHintStat> stats = new ArrayList<>();
        for( AngleHint hint: hints){

            Geometry before = this.currentPossibleArea;
            log.info("current possible size" + before.getArea());
            Geometry after = gameField.testHint(hint);
            AngleHintStat hs = new AngleHintStat(hint,before,after);

            //calc some Statistics
            fillDistanceToNormalLine(hs);
            fillDistanceToCentroid(hs);

            stats.add(hs);
            rateHint(hs);
        }
        log.debug("#of hints" + stats.size());
        stats = filterForValidHints(stats);
        log.debug("#of hints" + stats.size());
        stats.sort(Comparator.comparingDouble(AngleHintStat::getRating).reversed());
        //stats.stream().map(s -> s.toString());

        AngleHintStat returnHint = stats.get(0);

        log.info("eval angleHint");
        log.info(returnHint.toString());
        //add some statusinformation
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

    private void fillDistanceToNormalLine(AngleHintStat ahs){
        Coordinate center = ahs.getHint().getGeometryAngle().getCenter();
        Coordinate middlePoint = JTSUtils.middleOfAngleHint(ahs.getHint());
        LineSegment normalLine = new LineSegment(center,middlePoint);

        double distToNormal = normalLine.project(treasure.getCoordinate()).distance(treasure.getCoordinate());
        ahs.setDistanceFromNormalAngleLineToTreasure(distToNormal);
    }

    private void fillDistanceToCentroid(AngleHintStat ahs){
        Geometry after = ahs.getAreaAfterHint();
        Point centroid = after.getCentroid();
        log.info("Centroid " + centroid);

        Coordinate[] x = new Coordinate[]{ new Coordinate(10,10),new Coordinate(-10,10), new Coordinate(-10,-10), new Coordinate(10,-10), new Coordinate(10,10)};
        Geometry test = gf.createPolygon(x);
        Point tCentroid = test.getCentroid();

        GeometryStyle centroidStyle = new GeometryStyle(true, new Color(244,149,41));
        GeometryItem<Point> centroidItem = new GeometryItem(centroid, GeometryType.CENTROID , centroidStyle);
        ahs.getHint().addAdditionalItem(centroidItem);

        double dist = centroid.distance(treasure);
        ahs.setDistanceFromResultingCentroidToTreasure(dist);
    }

    /**
     * Rates the Given Hint with a custom function, saves the result in the AngleHintStat - Wrapper and returns the result
     * @param ahs The AngleHint - Wrapper , in which the result is saved
     * @return the rating of the hint
     */
    private double rateHint(AngleHintStat ahs){
        double rating = 0;

        rating += 10 * ( 1 / ahs.getRelativeAreaCutoff());
        rating += 0 * ahs.getDistanceFromNormalAngleLineToTreasure();
        rating += 1 * ahs.getDistanceFromResultingCentroidToTreasure();

        ahs.setRating(rating);
        return rating;
    }


    private List<AngleHintStat> filterForValidHints(List<AngleHintStat> stats){

        stats = stats.stream().filter(hint -> hint.getHint().getGeometryAngle().inView(treasure.getCoordinate())).collect(Collectors.toList());
        log.debug("remaining possible Hints" + stats.size());
        return stats;
    }

    @Override
    public Point getTreasureLocation(){
        return this.treasure;
    }
}
