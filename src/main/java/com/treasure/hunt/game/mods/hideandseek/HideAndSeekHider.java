package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;

/**
 * This kind of {@link Hider} could change the position of the treasure
 * after each move. Like in hide and seek.
 *
 * @param <T> the type of {@link Hint} this {@link Hider} can handle.
 * @author dorianreineccius
 */
public interface HideAndSeekHider<T extends Hint> extends Hider<T> {
}
