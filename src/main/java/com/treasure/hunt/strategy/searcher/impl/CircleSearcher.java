package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

public class CircleSearcher implements Searcher<CircleHint> {

    Coordinate origin;

    @Override
    public void init(Point searcherStartPosition) {
        origin = searcherStartPosition.getCoordinate();
    }

    @Override
    public SearchPath move() {
        return new SearchPath(JTSUtils.createPoint(origin));
    }

    @Override
    public SearchPath move(CircleHint hint) {
        Circle circle = hint.getCircle();

        Coordinate nextPosition = circle.intersection(origin);

        if (isGlobalGreedy()) {
            origin = nextPosition;
        }

        return new SearchPath(JTSUtils.createPoint(nextPosition));
    }

    private boolean isGlobalGreedy() {
        return true;
    }
}
