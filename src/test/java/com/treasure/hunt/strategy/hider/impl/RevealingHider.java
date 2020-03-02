package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * This {@link com.treasure.hunt.strategy.hider.Hider} always tells the treasurePosition
 * based on an {@link CircleHint} with radius 0.0.
 *
 * @author dorianreineccius
 */
public class RevealingHider implements HideAndSeekHider<CircleHint> {
    private GeometryFactory geometryFactory = new GeometryFactory();
    private Point treasurePos = geometryFactory.createPoint(new Coordinate(45, 45));

    @Override
    public void init(Point searcherStartPosition) {

    }

    /**
     * @return A {@link CircleHint} telling the exact treasure position
     */
    @Override
    public CircleHint move(SearchPath searchPath) {
        return new CircleHint(treasurePos, 0);
    }

    /**
     * @return the current treasure position
     */
    @Override
    public Point getTreasureLocation() {
        return treasurePos;
    }
}
