package com.treasure.hunt.view.custom;

import javafx.scene.control.Skin;

public class CoinProgress extends CoinLoader {
    @Override
    protected Skin<CoinLoader> createDefaultSkin() {
        return new CoinProgressSkin(this);
    }
}
