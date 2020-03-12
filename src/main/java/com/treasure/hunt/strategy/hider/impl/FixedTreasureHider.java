package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FixedTreasureHider extends StatisticalHider implements Hider<AngleHint> {

    private Point treasure = this.gf.createPoint(new Coordinate( 70,70));


    @Override
    public AngleHint eval(List<AngleHint> hints) {

        List<AngleHintStat> stats = new ArrayList<>();
        for( AngleHint hint: hints){

            Geometry before = this.currentPossibleArea;
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
        return stats.get(0).getHint();
    }

    private void fillDistanceToNormalLine(AngleHintStat ahs){
        Coordinate center = ahs.getHint().getGeometryAngle().getCenter();
        Coordinate middlePoint = JTSUtils.middleOfAngleHint(ahs.getHint());
        LineSegment normalLine = new LineSegment(center,middlePoint);

        double distToNormal = normalLine.project(treasure.getCoordinate()).distance(treasure.getCoordinate());
        ahs.setDistanceFromNormalAngleLineToTreasure(distToNormal);
    }

    private void fillDistanceToCentroid(AngleHintStat ahs){
        Coordinate centroid = ahs.getAreaAfterHint().getCoordinate();
        double dist = centroid.distance(treasure.getCoordinate());
        ahs.setDistanceFromResultingCentroidToTreasure(dist);
    }

    /**
     * Rates the Given Hint with a custom function, saves the result in the AngleHintStat - Wrapper and returns the result
     * @param ahs The AngleHint - Wrapper , in which the result is saved
     * @return the rating of the hint
     */
    private double rateHint(AngleHintStat ahs){
        double rating = 0;

        rating += 1 * ( 1 / ahs.getRelativeAreaCutoff());
        rating += 0 * ahs.getDistanceFromNormalAngleLineToTreasure();
        rating += 0 * ahs.getDistanceFromResultingCentroidToTreasure();

        ahs.setRating(rating);
        return rating;
    }


    private List<AngleHintStat> filterForValidHints(List<AngleHintStat> stats){
        log.debug("FILTERING " + stats.stream().filter(hint -> hint.getHint().getGeometryAngle().inView(treasure.getCoordinate())).count());
        stats = stats.stream().filter(hint -> hint.getHint().getGeometryAngle().inView(treasure.getCoordinate())).collect(Collectors.toList());
        log.debug("statsize" + stats.size());
        return stats;
    }

    @Override
    public Point getTreasureLocation(){
        return this.treasure;
    }
}
