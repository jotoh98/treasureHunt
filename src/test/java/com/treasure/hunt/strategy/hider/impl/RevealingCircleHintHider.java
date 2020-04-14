package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * This {@link com.treasure.hunt.strategy.hider.Hider} always tells the treasurePosition
 * based on an {@link CircleHint} with radius 0.0.
 *
 * @author Dorian Reineccius
 */
public class RevealingCircleHintHider implements HideAndSeekHider<CircleHint> {
    private Point treasurePos;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Point searcherStartPosition) {
        treasurePos = JTSUtils.createPoint(new Coordinate(45, 45));
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
