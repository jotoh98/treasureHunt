package com.treasure.hunt.view.in_game;

import com.treasure.hunt.game.GameManager;

public interface View extends Runnable {

    /**
     * @param gameManager the {@link GameManager}, the View gets its inputs.
     */
    void init(GameManager gameManager);

}
