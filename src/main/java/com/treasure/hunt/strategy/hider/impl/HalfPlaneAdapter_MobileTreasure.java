package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import org.locationtech.jts.geom.Point;

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
