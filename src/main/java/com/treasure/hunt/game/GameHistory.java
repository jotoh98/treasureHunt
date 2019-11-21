package com.treasure.hunt.game;

import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.NoArgsConstructor;

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
    private List<Move> moves = Collections.synchronizedList(new ArrayList<>());
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private List<Runnable> views = new ArrayList<>();

    private int i = 0;

    /**
     * @param runnable the views, which gets registered.
     */
    public void registerListener(Runnable runnable) {
        views.add(runnable);
    }

    /**
     * This will run the views, e.g. start the run() method.
     */
    public void runListeners() {
        views.forEach(runnable -> executorService.execute(runnable));
    }

    public synchronized void previous() {
        if (i == 0) {
            throw new IllegalStateException("i == 0");
        }
        i--;
        runListeners();
    }

    public synchronized void next() {
        if (i == moves.size()) {
            throw new IllegalStateException("i == moves.size()");
        }
        i++;
        runListeners();
    }

    /**
     * This dumps the new game created {@link Move} into this history
     * and notifies the views.
     *
     * @param move The new {@link Move}, arisen in the game.
     */
    public synchronized void dump(Move move) {
        i++;
        moves.add(move);
        runListeners();
    }

    /**
     * @return The whole List of geometryItems of the gameHistory
     */
    public List<GeometryItem> getGeometryItems() {
        ArrayList<GeometryItem> geometryItems = moves.subList(0, i).stream()
                .flatMap(move -> move.getGeometryItems().stream()).collect(Collectors.toCollection(ArrayList::new));
        return geometryItems;
    }
}
