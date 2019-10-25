package com.treasure.hunt.strategy.moves;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import org.locationtech.jts.geom.Point;

import java.util.List;


public interface Seeker<T extends Hint> {

    /**
     * Use this to initialize your Seeker.
     * This will assign him a starting position and give him the GameHistory.
     *
     * @param startPosition
     * @param gameHistory
     */
    void init(Point startPosition, GameHistory gameHistory);

    Moves generate(T moves, Point currentLocation);

    Moves getMoves(T hint, Point currentLocation);

    List<GeometryType> getAvailableVisualisationGeometryTypes();

    String getDisplayName();
}
