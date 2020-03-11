package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FixedTreasureHider extends StatisticalHider {

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
        }
        stats.sort(Comparator.comparingDouble(AngleHintStat::getRating).reversed());

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

    private double rateHint(AngleHintStat ahs){
        double rating = 0;

        rating += 1 * ( 1 / ahs.getRelativeAreaCutoff());
        rating += 0 * ahs.getDistanceFromNormalAngleLineToTreasure();
        rating += 0 * ahs.getDistanceFromResultingCentroidToTreasure();

        ahs.setRating(rating);
        return rating;
    }

    @Override
    public Point getTreasureLocation() {
        return null;
    }
}
