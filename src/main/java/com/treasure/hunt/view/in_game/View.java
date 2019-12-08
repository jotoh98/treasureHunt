package com.treasure.hunt.view.in_game;

import com.treasure.hunt.game.GameManager;

/**
 * The View concurrently accesses the {@link GameManager}
 * and displays its {@link com.treasure.hunt.game.Move} objects.
 *
 * @author dorianreineccius
 */
public interface View extends Runnable {

    /**
     * @param gameManager the {@link GameManager}, the View gets its {@link com.treasure.hunt.game.Move} objects.
     */
    void init(GameManager gameManager);

}
