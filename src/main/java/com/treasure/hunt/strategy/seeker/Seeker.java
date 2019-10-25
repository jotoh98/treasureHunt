package com.treasure.hunt.strategy.seeker;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.Hint;
import org.locationtech.jts.geom.Point;

import java.util.List;


public interface Seeker<T extends Hint> {

    /**
     * Use this to receive the starting position for the player.
     *
     * @param position The starting position of the player.
     */
    void init(Point position);


    /**
     * Use this to choose some smart moves, without any hints.
     * At the beginning, you may get no hints.
     *
     * @return Moves
     */
    Moves generate();

    /**
     * Use this to choose some smart moves, with a hint given.
     *
     * @return Moves
     */
    Moves generate(T moves);

    /**
     * Use this, to output any visuals
     *
     * @return Visualization Gemoetry Items
     */
    List<GeometryItem> getAvailableVisualisationGeometryItems();

    /**
     * Use this to return your chosen name to display.
     *
     * @return Strategy name.
     */
    String getDisplayName();
}
