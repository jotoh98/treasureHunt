package com.treasure.hunt.strategy.hint;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.moves.Moves;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface Tipster<T extends Hint> {

    /**
     * Use this to initialize your Tipster.
     * This will give him the GameHistory.
     *
     * @param gameHistory
     */
    void init(GameHistory gameHistory);

    abstract T generate(Moves moves);

    T generateHint(Moves moves);

    List<GeometryType> getAvailableVisualisationGeometryTypes();

    String getDisplayName();
}