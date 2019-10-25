package com.treasure.hunt.ui.in_game;

import com.treasure.hunt.strategy.hint.Product;

public interface View {

    /**
     * Draw the product
     *
     * @param product The visuals to draw.
     */
    void visualizeProduct(Product product);
}
