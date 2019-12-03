package com.treasure.hunt.view.in_game.impl;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.view.in_game.View;

/**
 * The DummyView is created for concurrency testing
 * on shared memory.
 *
 * @author dorianreineccius
 */
public class DummyView implements View {
    private GameManager gameManager;

    /**
     * @param gameManager the {@link GameManager}, the View gets its {@link com.treasure.hunt.game.Move} objects.
     */
    @Override
    public void init(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * Access the shared memory.
     */
    @Override
    public void run() {
        gameManager.getGeometryItems();
    }
}
