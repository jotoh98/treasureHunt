package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;

@Slf4j
@Preference(name = PreferenceService.HintSize_Preference, value = 180)
@Preference(name = PreferenceService.TreasureLocationX_Preference, value = 70)
@Preference(name = PreferenceService.TreasureLocationY_Preference, value = 70)
public class FixedTreasureHider extends StatisticalHider implements Hider<AngleHint> {
    @Override
    protected double rateHint(AngleHintStatistic ahs) {
        double rating = 0;

        rating += 10 * (1 / ahs.getRelativeAreaCutoff());
        rating += 1 * ahs.getDistanceFromNormalAngleLineToTreasure();
        rating += 3 * ahs.getDistanceFromResultingCentroidToTreasure();

        ahs.setRating(rating);
        return rating;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point getTreasureLocation() {
        return this.treasure;
    }
}
