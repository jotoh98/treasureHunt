package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.geom.*;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.Preference;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

@Slf4j
@Preference(name = StatisticalHider.HintSize_Preference, value = 180)
@Preference(name = StatisticalHider.TreasureLocationX_Preference, value = 70)
@Preference(name = StatisticalHider.TreasureLocationY_Preference, value = 70)
public class FixedTreasureHider extends StatisticalHider implements Hider<AngleHint> {

    @Override
    protected double rateHint(AngleHintStat ahs){
        double rating = 0;

        rating += 10 * ( 1 / ahs.getRelativeAreaCutoff());
        rating += 1 * ahs.getDistanceFromNormalAngleLineToTreasure();
        rating += 3 * ahs.getDistanceFromResultingCentroidToTreasure();

        ahs.setRating(rating);
        return rating;
    }

    @Override
    public Point getTreasureLocation(){
        return this.treasure;
    }
}
