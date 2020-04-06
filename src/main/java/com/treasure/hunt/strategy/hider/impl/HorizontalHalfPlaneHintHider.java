package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * This kind of {@link Hider} only gives {@link HalfPlaneHint}'s,
 * which are parallel to the X-axis.
 */
public class HorizontalHalfPlaneHintHider implements Hider<HalfPlaneHint> {
    private Point treasurePosition;

    /**
     * This initialization does nothing and ignores the {@code searcherStartPosition}.
     *
     * @param searcherStartPosition the {@link com.treasure.hunt.strategy.searcher.Searcher} starting position
     */
    @Override
    public void init(Point searcherStartPosition) {
    }

    /**
     * @param searchPath the {@link SearchPath}, the {@link com.treasure.hunt.strategy.searcher.Searcher} did last
     * @return A valid {@link HalfPlaneHint} which are parallel to the X-axis.
     */
    @Override
    public HalfPlaneHint move(SearchPath searchPath) {
        Point searcherPoint = searchPath.getLastPoint();
        if (searcherPoint.getY() < treasurePosition.getY()) {
            return new HalfPlaneHint(searcherPoint.getCoordinate(), new Coordinate(searcherPoint.getX() + 1, searcherPoint.getY()));
        } else {
            return new HalfPlaneHint(searcherPoint.getCoordinate(), new Coordinate(searcherPoint.getX() - 1, searcherPoint.getY()));
        }
    }

    /**
     * @return A random treasure location via {@link JTSUtils#shuffleTreasure()}.
     */
    @Override
    public Point getTreasureLocation() {
        treasurePosition = JTSUtils.shuffleTreasure();
        return treasurePosition;
    }
}
