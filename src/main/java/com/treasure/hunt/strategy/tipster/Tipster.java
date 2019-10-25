package com.treasure.hunt.strategy.tipster;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.seeker.Moves;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface Tipster<T extends Hint> {

    /**
     * This defines the probability, that the hint
     * given by the hintGen really contain the treasure.
     *
     * @param insecurity The probability, that the given hint is correct.
     */
    void init(double insecurity);

    /**
     * Use this to choose some smart hint, given no Moves of the player.
     *
     * @return Moves
     */
    T generateHint();

    /**
     * Use this to choose some smart hint, given the Moves of the player.
     *
     * @return Moves
     */
    T generateHint(Moves moves);

    /**
     * Use this, to output any visuals
     *
     * @return Visualization Gemoetry Items
     */
    List<GeometryItem> getAvailableVisualisationGeometryItems();

    /**
     * Use this to return your chosen Strategy name to display.
     *
     * @return Strategy name.
     */
    String getDisplayName();

    /**
     * Use this to return the actual treasure location.
     * This may be switched.
     *
     * @return Point treasure location
     */
    Point getTreasureLocation();
}