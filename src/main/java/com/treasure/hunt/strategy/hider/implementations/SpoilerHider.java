package com.treasure.hunt.strategy.hider.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.strategy.hint.CircleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * The SpoilerHider always tells the treasurePosition
 * based on an {@link CircleHint} with radius 0.0.
 */
public class SpoilerHider implements HideAndSeekHider<CircleHint> {

    private GeometryFactory geometryFactory = new GeometryFactory();
    private GameHistory gameHistory;
    private Point treasurePos = geometryFactory.createPoint(new Coordinate(45, 45));

    public Point getTreasurePos() {
        return treasurePos;
    }

    @Override
    public Point getTreasureLocation() {
        return treasurePos;
    }

    @Override
    public void init(GameHistory gameHistory) {
        this.gameHistory = gameHistory;
    }

    /**
     * @return spoiler
     */
    @Override
    public CircleHint move(Moves moves) {
        return new CircleHint(treasurePos, 0);
    }
}
