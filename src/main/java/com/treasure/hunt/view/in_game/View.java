package com.treasure.hunt.view.in_game;

import com.treasure.hunt.game.GameHistory;

/**
 * The View interface is the input/output
 */
public interface View extends Runnable {

    /**
     * @param gameHistory the {@link GameHistory}, the View gets its inputs.
     */
    void init(GameHistory gameHistory);

}
