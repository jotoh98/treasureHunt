package com.treasure.hunt.game;

import com.treasure.hunt.strategy.geom.GeometryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will be accessed by differend threads.
 * Thus, the shared variables needs to get locked!
 */
public class GameHistory {
    List<GeometryItem> history;

    public GameHistory() {

    }

    /**
     * This will allow only one Thread to access this method.
     *
     * @param geometryItems
     */
    public synchronized void addItems(List<GeometryItem> geometryItems) {
        this.history.addAll(geometryItems);
    }

    /**
     * This will allow only one Thread to access this method.
     * Nevertheless, the GeometryItems could be accessed from both Thread at the same time.
     * But after the GameSimulation uses to only create but not read them, this will not happen.
     *
     * @return List<GeometryItem>
     */
    public synchronized List<GeometryItem> readItems() {
        List<GeometryItem> list = new ArrayList<GeometryItem>();
        list.addAll(history);
        return list;
    }
}
