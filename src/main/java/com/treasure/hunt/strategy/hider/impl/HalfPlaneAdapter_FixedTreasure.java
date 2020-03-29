package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.jts.geom.HalfPlane;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import org.locationtech.jts.geom.Point;

public class HalfPlaneAdapter_FixedTreasure implements Hider<HalfPlaneHint> {

    private FixedTreasureHider adaptee;
    public HalfPlaneAdapter_FixedTreasure(){
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
