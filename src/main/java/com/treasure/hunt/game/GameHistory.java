package com.treasure.hunt.game;

import com.treasure.hunt.strategy.Product;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor
public class GameHistory {
    /**
     * Locks GameHistory.products, such that it can only be accessed by one thread
     * at the same time.
     */
    private List<Product> products = Collections.synchronizedList(new ArrayList<>());
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private List<Runnable> listeners = new ArrayList<>();

    private void registerListener(Runnable runnable) {
        listeners.add(runnable);
    }

    /**
     * This dumps the new game created {@link Product}
     * into this history.
     *
     * @param product The new Product, arised in the game.
     */
    public void dump(Product product) {
        products.add(product);
        listeners.forEach(runnable -> executorService.execute(runnable));
    }

    /**
     * This returns a copy of the productsList.
     * The Product objects are still the same!
     *
     * @return copy of the productList.
     */
    public List<Product> giveProductsCopy() {
        return new ArrayList<Product>(products);
    }

}
