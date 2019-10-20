package com.treasure.hunt.strategy.search.implemenations;

import com.treasure.hunt.strategy.hint.hints.AngleHint;
import com.treasure.hunt.strategy.search.AbstractSearchStrategy;
import org.locationtech.jts.geom.Point;

import java.util.List;

public class StrategyFromPaper extends AbstractSearchStrategy<AngleHint> {

    @Override
    public void init() {
        //TODO: implement
    }

    @Override
    public List<Point> getNextMoves(AngleHint hint, Point currentLocation) {
        //TODO: implement
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Strategy from Paper";
    }
}
