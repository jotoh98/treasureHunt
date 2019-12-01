package com.treasure.hunt.view.in_game.impl;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.view.in_game.View;

public class DummyView implements View {
    private GameManager gameManager;

    @Override
    public void init(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        gameManager.getGeometryItems();
    }
}
