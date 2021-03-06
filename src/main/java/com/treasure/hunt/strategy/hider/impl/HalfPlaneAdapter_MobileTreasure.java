package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import org.locationtech.jts.geom.Point;

@Preference(name = PreferenceService.HintSize_Preference, value = 180)
@Preference(name = GameField.CircleExtension_Preference, value = 0)
@Preference(name = StatisticalHider.relativeAreaCutoffWeight_Preference, value = 5)
@Preference(name = StatisticalHider.DistanceFromNormalAngleLineToTreasureWeight_Preference, value = 2)
@Preference(name = StatisticalHider.DistanceFromResultingCentroidToTreasureWeight_Preference, value = 0.2)
@Preference(name = MobileTreasureHider.treasureBeforeHintFirst_Preference, value = 1)
@Preference(name = PreferenceService.TREASURE_DISTANCE, value = 100)
@Preference(name = MobileTreasureHider.walkedPathLengthForTreasureRelocation_Preference, value = 1)
@Preference(name = MobileTreasureHider.mindTreasureRelocationDistance_Preference, value = 15)
@Preference(name = MobileTreasureHider.badHintWeight_Preference, value = 15)
public class HalfPlaneAdapter_MobileTreasure implements HideAndSeekHider<HalfPlaneHint> {
    private MobileTreasureHider adaptee;

    public HalfPlaneAdapter_MobileTreasure() {
        adaptee = new MobileTreasureHider();
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
