package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

public class CircleSearcher implements Searcher<CircleHint> {

    public void init(Point startPosition) {

    }

    @Override
    public Movement move() {
        return null;
    }

    @Override
    public Movement move(CircleHint hint) {
        return null;
    }
}
