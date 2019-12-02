package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * The SpoilerHider always tells the treasurePosition
 * based on an {@link CircleHint} with radius 0.0.
 */
public class RevealingHider implements HideAndSeekHider<CircleHint> {

    private GeometryFactory geometryFactory = new GeometryFactory();
    private Point treasurePos = geometryFactory.createPoint(new Coordinate(45, 45));

    @Override
    public Point getTreasureLocation() {
        return treasurePos;
    }

    /**
     * @return A {@link CircleHint} telling the exact treasure position
     */
    @Override
    public CircleHint move(Movement movement) {
        return new CircleHint(treasurePos, 0);
    }
}
