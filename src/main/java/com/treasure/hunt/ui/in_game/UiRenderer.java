package com.treasure.hunt.ui.in_game;

import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;

public abstract class UiRenderer {

    public void visualizeProduct(Product product) {
        product.getGeometryItems().forEach(this::drawItem);
    }

    protected abstract void drawItem(GeometryItem geometryItem);
}
