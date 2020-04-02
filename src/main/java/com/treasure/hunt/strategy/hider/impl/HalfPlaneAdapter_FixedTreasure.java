package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import org.locationtech.jts.geom.Point;

/**
 * Adapter such that searchers who require HalfPlaneHints can play against this strategy
 * <p>
 * Unfortunately some redundant work needs to be done ( e.g. Preferences set twice in the code)
 */
@Preference(name = PreferenceService.HintSize_Preference, value = 180)
@Preference(name = GameField.CircleExtension_Preference, value = 0)
@Preference(name = StatisticalHider.getRelativeAreaCutoffWeight_Preference, value = 10)
@Preference(name = StatisticalHider.DistanceFromNormalAngleLineToTreasureWeight_Preference, value = 1)
@Preference(name = StatisticalHider.DistanceFromResultingCentroidToTreasureWeight_Preference, value = 3)
@Preference(name = PreferenceService.MAX_TREASURE_DISTANCE, value = 100)
public class HalfPlaneAdapter_FixedTreasure implements Hider<HalfPlaneHint> {

    private FixedTreasureHider adaptee;

    public HalfPlaneAdapter_FixedTreasure() {
        adaptee = new FixedTreasureHider();
    }

    @Override
    public void init(Point searcherStartPosition) {
        //PreferenceService.getInstance().putPreference();
        adaptee.init(searcherStartPosition);
    }

    @Override
    public HalfPlaneHint move(SearchPath searchPath) {
        HalfPlaneHint castedHint = (HalfPlaneHint) adaptee.move(searchPath);
        return castedHint;
    }

    @Override
    public Point getTreasureLocation() {
        return adaptee.getTreasureLocation();
    }
}
