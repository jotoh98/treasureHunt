package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import org.locationtech.jts.geom.Point;

/**
 * This kind of {@link Hider} could change the position of the treasure
 * after each move. Like in hide and seek.
 */
public interface HideAndSeekHider<T extends Hint> extends Hider<T> {

    /**
     * This should output the current treasure location,
     * the {@link Hider} is able to change.
     *
     * @return Point the new treasure location
     */
    Point getTreasureLocation();
}