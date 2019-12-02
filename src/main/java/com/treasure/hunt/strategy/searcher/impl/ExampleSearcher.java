package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

/**
 * @author axel12
 */
public class ExampleSearcher implements Searcher<AngleHint> {
    @Override
    public void init(Point startPosition) {
    }

    @Override
    public Movement move() {
        return null;
    }

    @Override
    public Movement move(AngleHint hint) {
        return null;
    }
}
