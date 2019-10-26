package com.treasure.hunt.view.in_game;

import com.treasure.hunt.strategy.Product;

public interface View extends Runnable{

    /**
     * Draw the product
     *
     * @param product The visuals to draw.
     */
    void visualizeProduct(Product product);
}
