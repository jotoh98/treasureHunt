package com.treasure.hunt.strategy.search.implemenations;

import com.treasure.hunt.strategy.hint.hints.AngelByPointHint;
import com.treasure.hunt.strategy.search.AbstractSearchStrategy;
import org.locationtech.jts.geom.Point;

import java.util.List;

public class StrategyFromPaper extends AbstractSearchStrategy<AngelByPointHint> {

    @Override
    public void init() {
        //TODO: implement
    }

    @Override
    public List<Point> getNextMoves(AngelByPointHint hint, Point currentLocation) {
        //TODO: implement
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Strategy from Paper";
    }
}
