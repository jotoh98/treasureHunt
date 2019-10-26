package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hint.Product;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@NoArgsConstructor
public class GameHistory {
    /**
     * Locks GameHistory.products, such that it can only be accessed by one thread
     * at the same time.
     */
    private Lock productsLock = new ReentrantLock();

    private List<Product> products;

    /**
     * This dumps the new game created {@link Product}
     * into this history.
     *
     * @param product The new Product, arised in the game.
     */
    public void dump(Product product) {
        try {
            productsLock.lock();
            products.add(product);
        } finally {
            productsLock.unlock();
        }
    }

    /**
     * This returns a copy of the productsList.
     * The Product objects are still the same!
     *
     * @return copy of the productList.
     */
    public List<Product> giveProductsCopy() {
        List<Product> copy = new ArrayList<>();
        try {
            productsLock.lock();
            copy.addAll(products);
        } finally {
            productsLock.unlock();
        }
        return copy;
    }

}
