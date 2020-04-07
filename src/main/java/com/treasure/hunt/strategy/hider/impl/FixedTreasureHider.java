package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.StatusMessageItem;
import com.treasure.hunt.strategy.geom.StatusMessageType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;

import java.text.DecimalFormat;

/**
 * A Strategy using the {@link StatisticalHider} as Base Implementation, using the {@link GameField} to maintain its state
 */
@Slf4j
@Preference(name = PreferenceService.HintSize_Preference, value = 180)
@Preference(name = PreferenceService.TREASURE_DISTANCE, value = 100)
@Preference(name = GameField.CircleExtension_Preference, value = 0)
@Preference(name = StatisticalHider.relativeAreaCutoffWeight_Preference, value = 10)
@Preference(name = StatisticalHider.DistanceFromNormalAngleLineToTreasureWeight_Preference, value = 1)
@Preference(name = StatisticalHider.DistanceFromResultingCentroidToTreasureWeight_Preference, value = 3)
@Preference(name = PreferenceService.MAX_TREASURE_DISTANCE, value = 100)
@Preference(name = MobileTreasureHider.badHintWeight_Preference, value = 15)
public class FixedTreasureHider extends StatisticalHider implements Hider<AngleHint> {
    @Override
    protected double rateHint(AngleHintStatistic ahs) {
        double rating = 0;
        double ratingAddition;

        double inverseRelativeAreaCutoff;
        if( ahs.getRelativeAreaCutoff() == 0) {
            inverseRelativeAreaCutoff = 5000;
        }else{
            inverseRelativeAreaCutoff = (1 / ahs.getRelativeAreaCutoff());
        }

        ratingAddition = PreferenceService.getInstance().getPreference(StatisticalHider.relativeAreaCutoffWeight_Preference, 5).doubleValue() * inverseRelativeAreaCutoff;
        rating += ratingAddition;
        String relativeArea_rating = new DecimalFormat("#.00").format(ratingAddition);
        ahs.getHint().getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.RELATIVE_AREA_CUTOFF_RATING, relativeArea_rating));


        ratingAddition = PreferenceService.getInstance().getPreference(StatisticalHider.DistanceFromNormalAngleLineToTreasureWeight_Preference, 2).doubleValue() * ahs.getDistanceFromNormalAngleLineToTreasure();
        rating += ratingAddition;
        String angleBisector_rating = new DecimalFormat("#.00").format(ratingAddition);
        ahs.getHint().getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.DISTANCE_ANGLE_BISECTOR_RATING, angleBisector_rating));

        ratingAddition += PreferenceService.getInstance().getPreference(StatisticalHider.DistanceFromResultingCentroidToTreasureWeight_Preference, 3).doubleValue() * ahs.getDistanceFromResultingCentroidToTreasure();
        rating += ratingAddition;
        String centroidDistance_rating = new DecimalFormat("#.00").format(ratingAddition);
        ahs.getHint().getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.DISTANCE_TREASURE_TO_CENTROID_RATING, centroidDistance_rating));

        if( ahs.isBadHint() ){
            ratingAddition = PreferenceService.getInstance().getPreference(StatisticalHider.badHintWeight_Preference, 2).doubleValue() * 1;
            rating += ratingAddition;
            String hintQuality_rating = new DecimalFormat("#.00").format(ratingAddition);
            ahs.getHint().getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.HINT_QUALITY_HIDER_RATING , hintQuality_rating));
        }

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
