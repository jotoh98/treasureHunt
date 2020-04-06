package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;

/**
 * A Strategy using the {@link StatisticalHider} as Base Implementation, using the {@link GameField} to maintain its state
 */
@Slf4j
@Preference(name = PreferenceService.HintSize_Preference, value = 180)
@Preference(name = GameField.CircleExtension_Preference, value = 0)
@Preference(name = StatisticalHider.getRelativeAreaCutoffWeight_Preference, value = 10)
@Preference(name = StatisticalHider.DistanceFromNormalAngleLineToTreasureWeight_Preference, value = 1)
@Preference(name = StatisticalHider.DistanceFromResultingCentroidToTreasureWeight_Preference, value = 3)
@Preference(name = PreferenceService.MAX_TREASURE_DISTANCE, value = 100)
public class FixedTreasureHider extends StatisticalHider implements Hider<AngleHint> {
    @Override
    protected double rateHint(AngleHintStatistic ahs) {
        double rating = 0;

        rating += PreferenceService.getInstance().getPreference(StatisticalHider.getRelativeAreaCutoffWeight_Preference, 10).doubleValue() * (1 / ahs.getRelativeAreaCutoff());
        rating += PreferenceService.getInstance().getPreference(StatisticalHider.DistanceFromNormalAngleLineToTreasureWeight_Preference, 1).doubleValue() * ahs.getDistanceFromNormalAngleLineToTreasure();
        rating += PreferenceService.getInstance().getPreference(StatisticalHider.DistanceFromResultingCentroidToTreasureWeight_Preference, 3).doubleValue() * ahs.getDistanceFromResultingCentroidToTreasure();

        ahs.setRating(rating);
        return rating;
    }

    /**
     * This method will be invoked at the start of the Game. it returns the initial treasure location.
     * Since hider does not move the treasure, any further calls to this method will return the same result.
     *
     * @return the point the treasure is located at
     */
    @Override
    public Point getTreasureLocation() {
        return this.treasure;
    }
}
