package com.treasure.hunt.view.in_game;

import com.treasure.hunt.strategy.geom.GeometryItem;

public interface View<T> extends Runnable {

    /**
     * Draw the product
     *
     * @param geometryItem The visuals to draw.
     */
    T transfer(GeometryItem geometryItem);
}
