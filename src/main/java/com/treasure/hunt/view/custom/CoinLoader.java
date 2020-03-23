package com.treasure.hunt.view.custom;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Skin;

public class CoinLoader extends ProgressIndicator {

    @Override
    protected Skin<CoinLoader> createDefaultSkin() {
        return new CoinLoaderSkin(this);
    }
}
