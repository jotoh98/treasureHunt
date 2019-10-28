package com.treasure.hunt.strategy.hider;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

public interface Hider<T extends Hint> {

    /**
     * Use this to initialize your hider.
     * This will tell him the treasure position and
     * give him the GameHistory.
     *
     * @param treasurePosition The initial treasure position
     * @param gameHistory The gameHistory, the hider dumps its geometry objects by {@link #commitProduct(Product)}
     */
    void init(Point treasurePosition, GameHistory gameHistory);

    /**
     * This should let the hider commit all of his created GeometryItems
     * to the View-Thread.
     * For this, you could use void {@link GameHistory#dump(Product)}
     *
     * @param product The product of geometry objects, the view should display
     */
    void commitProduct(Product product);

    /**
     * Use this to tell a hint,
     * knowing the last Moves of the {@link Searcher}.
     *
     * @param moves the moves, the {@link Searcher} did last.
     * @return T a hint.
     */
    T move(Moves moves);
}