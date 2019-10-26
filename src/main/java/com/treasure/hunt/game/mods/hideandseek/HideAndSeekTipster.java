package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.strategy.tipster.Tipster;
import org.locationtech.jts.geom.Point;

/**
 * This kind of {@link Tipster} could change the position of the treasure
 * after each move.
 * Imaging an excaping {@link Tipster}, being haunted by the Seeker.
 * Also a hide and seek game would be conceivable.
 */
public interface HideAndSeekTipster extends Tipster {

    /**
     * This should output the current treasure location,
     * the tipster is able to change.
     *
     * @return Point the new treasure location
     */
    Point getTreasureLocation();
}
