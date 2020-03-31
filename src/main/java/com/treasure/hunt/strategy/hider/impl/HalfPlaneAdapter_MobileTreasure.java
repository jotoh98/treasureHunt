package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import org.locationtech.jts.geom.Point;

@Preference(name = PreferenceService.HintSize_Preference, value = 180)
@Preference(name = GameField.CircleExtension_Preference, value = 0)
@Preference(name = StatisticalHider.getRelativeAreaCutoffWeight_Preference, value = 5)
@Preference(name = StatisticalHider.DistanceFromNormalAngleLineToTreasureWeight_Preference, value = 2)
@Preference(name = StatisticalHider.DistanceFromResultingCentroidToTreasureWeight_Preference, value = 3)
@Preference(name = MobileTreasureHider.treasureBeforeHintFirst_Preference, value = 1)
@Preference(name = PreferenceService.MAX_TREASURE_DISTANCE, value = 100)
public class HalfPlaneAdapter_MobileTreasure implements HideAndSeekHider<HalfPlaneHint> {
    private MobileTreasureHider adaptee;
    public HalfPlaneAdapter_MobileTreasure(){
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
