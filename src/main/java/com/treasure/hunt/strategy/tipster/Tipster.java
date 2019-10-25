package com.treasure.hunt.strategy.tipster;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.seeker.Moves;

import java.util.List;

public interface Tipster<T extends Hint> {

    /**
     * Use this to initialize your Tipster.
     * This will give him the GameHistory.
     *
     * @param gameHistory
     */
    void init(GameHistory gameHistory);

    /**
     * This should let the Tipster commit all of his created GeometryItems
     * to the View-Thread.
     */
    void commitProduct();

    T generate(Moves moves);

    T generateHint(Moves moves);

    String getDisplayName();
}