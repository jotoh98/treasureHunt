package com.treasure.hunt.view.in_game;

import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;

/**
 * The View class will be started as a new Thread,
 * in order to not bottleneck the GameSimulation runtime.
 */
public interface View extends Runnable {

    void visualizeProduct(Product product);

    void drawItem(GeometryItem geometryItem);
}
