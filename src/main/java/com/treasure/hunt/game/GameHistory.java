package com.treasure.hunt.game;

import com.treasure.hunt.strategy.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will be accessed by differend threads.
 * Thus, the shared variables needs to get locked!
 */
public class GameHistory {
    List<Product> history;

    public GameHistory() {

    }

    /**
     * This will allow only one Thread to access this method.
     *
     * @param product
     */
    public synchronized void addItems(Product product) {
        this.history.add(product);
    }

    /**
     * This will allow only one Thread to access this method.
     * Nevertheless, the GeometryItems could be accessed from both Thread at the same time.
     * But after the GameSimulation uses to only create but not read them, this will not happen.
     *
     * Every Product is a step, the Seeker or the Tipster did.
     *
     * @return List<Product>
     */
    public synchronized List<Product> readItems() {
        List<Product> list = new ArrayList<>();
        list.addAll(history);
        return list;
    }
}
