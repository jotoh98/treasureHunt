package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * This kind of {@link com.treasure.hunt.strategy.searcher.Searcher} only gives {@link HalfPlaneHint}'s,
 * which are parallel to the X-axis.
 */
public class BadHalfplaneHintHider implements Hider<HalfPlaneHint> {
    private Point treasurePosition;

    @Override
    public void init(Point searcherStartPosition) {
        return;
    }

    @Override
    public HalfPlaneHint move(SearchPath searchPath) {
        Point searcherPoint = searchPath.getLastPoint();
        if (searcherPoint.getY() < treasurePosition.getY()) {
            return new HalfPlaneHint(searcherPoint.getCoordinate(), new Coordinate(searcherPoint.getX() + 1, searcherPoint.getY()));
        } else {
            return new HalfPlaneHint(searcherPoint.getCoordinate(), new Coordinate(searcherPoint.getX() - 1, searcherPoint.getY()));
        }
    }

    @Override
    public Point getTreasureLocation() {
        treasurePosition = JTSUtils.shuffleTreasure();
        return treasurePosition;
    }
}
