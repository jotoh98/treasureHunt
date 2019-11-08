package com.treasure.hunt.strategy.hider;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

public interface Hider<T extends Hint> {

    /**
     * This should let the hider commit all of his created GeometryItems
     * to the View-Thread.
     * For this, you could use void {@link GameHistory#dump(Product)}
     *
     * @param product The product of geometry objects, the view should display
     */
    void commitProduct(Product product);

    /**
     * This should output the current treasure location,
     * the {@link Hider} is able to change.
     *
     * @return Point the new treasure location
     */
    Point getTreasureLocation();

    /**
     * Use this to initialize your hider.
     * This will give him the GameHistory.
     *
     * @param gameHistory The gameHistory, the hider dumps its geometry objects by {@link #commitProduct(Product)}
     */
    void init(GameHistory gameHistory);

    /**
     * Use this to tell a hint,
     * knowing the last Moves of the {@link Searcher}.
     *
     * @param moves the moves, the {@link Searcher} did last.
     * @return T a hint.
     */
    T move(Moves moves);
}