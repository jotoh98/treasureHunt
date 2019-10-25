package com.treasure.hunt.strategy.seeker;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.tipster.Hint;
import org.locationtech.jts.geom.Point;

public interface Seeker<T extends Hint> {

    /**
     * Use this to initialize your Seeker.
     * This will assign him a starting position and give him the GameHistory.
     *
     * @param startPosition
     * @param gameHistory
     */
    void init(Point startPosition, GameHistory gameHistory);

    /**
     * This should let the Seeker commit all of his created GeometryItems
     * to the View-Thread.
     */
    void commitProduct();

    Moves generate(T moves, Point currentLocation);

    Moves getMoves(T hint, Point currentLocation);

    String getDisplayName();
}
