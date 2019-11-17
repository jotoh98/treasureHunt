package com.treasure.hunt.game;

import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Moves;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@NoArgsConstructor
public class GameHistory {
    /**
     * Locks GameHistory.products, such that it can only be accessed by one thread
     * at the same time.
     */
    private List<Product> products = Collections.synchronizedList(new ArrayList<>());
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private List<Runnable> views = new ArrayList<>();
    private GeometryItem<Point> treasureLocation;

    /**
     * @param runnable the views, which gets registered.
     */
    public void registerListener(Runnable runnable) {
        views.add(runnable);
    }

    /**
     * This will run the views.
     */
    public void runListeners() {
        views.forEach(runnable -> executorService.execute(runnable));
    }

    /**
     * This dumps the new game created {@link Product} into this history
     * and notifies the views.
     *
     * @param product The new Product, arisen in the game.
     */
    public synchronized void dump(Hint product) {
        products.add(product);
        runListeners();
    }

    /**
     * This dumps the new game created {@link Product} into this history
     * and notifies the views.
     *
     * @param product The new Product, arisen in the game.
     */
    public synchronized void dump(Moves product) {
        products.add(product);
        runListeners();
    }

    public void dumpTreasureLocation(GeometryItem<Point> pointGeometryItem) {
        treasureLocation = pointGeometryItem;
        runListeners();
    }

    public List<GeometryItem> getGeometryItems() {
        ArrayList<GeometryItem> geometryItems = products.stream()
                .flatMap(product -> product.getGeometryItems().stream()).collect(Collectors.toCollection(ArrayList::new));
        geometryItems.add(treasureLocation);
        return geometryItems;
    }

}
