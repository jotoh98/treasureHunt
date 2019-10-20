package com.treasure.hunt.strategy.moves.implemenations;

import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.moves.AbstractMovesGenerator;
import com.treasure.hunt.strategy.moves.Moves;
import org.locationtech.jts.geom.Point;

public class StrategyFromPaper extends AbstractMovesGenerator<AngleHint> {

    @Override
    public void init() {
        //TODO: implement
    }

    @Override
    protected Moves generate(AngleHint hint, Point currentLocation) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Strategy from Paper";
    }
}
