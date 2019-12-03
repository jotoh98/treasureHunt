package com.treasure.hunt.game.mods.hideandseek;

import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Searcher;

/**
 * @param <T> the type of {@link Hint} this {@link Searcher} can handle.
 * @author dorianreineccius
 */
public interface HideAndSeekSearcher<T extends Hint> extends Searcher<T> {
}
