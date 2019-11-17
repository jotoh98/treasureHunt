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
    private List<Runnable> views = new ArrayList<>();

    /**
     * @param runnable the views, which gets registered.
     */
    public void registerListener(Runnable runnable) {
        views.add(runnable);
    }

    /**
     * This will run the views.
     */
    public void startListeners() {
        views.forEach(runnable -> executorService.execute(runnable));
    }

    /**
     * This dumps the new game created {@link Product} into this history
     * and notifies the views.
     *
     * @param product The new Product, arisen in the game.
     */
    public synchronized void dump(Product product) {
        products.add(product);
        notifyAll();
    }

    /**
     * @param i the last size, the {@link com.treasure.hunt.view.in_game.View} knew.
     * @return the new products, the {@link com.treasure.hunt.view.in_game.View} missed.
     */
    public synchronized List<Product> giveNewProducts(int i) throws InterruptedException {
        while (products.size() <= i) {
            wait();
        }
        List list = new ArrayList<>(products.subList(i, products.size() - 1));
        return list;
    }

}
