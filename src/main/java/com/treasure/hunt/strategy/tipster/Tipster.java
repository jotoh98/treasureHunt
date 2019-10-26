package com.treasure.hunt.strategy.tipster;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.seeker.Moves;
import com.treasure.hunt.strategy.seeker.Seeker;
import org.locationtech.jts.geom.Point;

public interface Tipster<T extends Hint> {

    /**
     * Use this to initialize your Tipster.
     * This will tell him the treasure position and
     * give him the GameHistory.
     *
     * @param treasurePosition The initial treasure position
     * @param gameHistory The gameHistory, the tipster dumps its geometry objects by Tipster.commitProduct
     */
    void init(Point treasurePosition, GameHistory gameHistory);

    /**
     * This should let the Tipster commit all of his created GeometryItems
     * to the View-Thread.
     * For this, you could use void dump(List<Product> list) from GameHistory.
     *
     * @param product The product of geometry objects, the view should display
     */
    void commitProduct(Product product);

    /**
     * Use this to tell a hint,
     * knowing the last Moves of the {@link Seeker}.
     *
     * @param moves the moves, the {@link Seeker} did last.
     * @return T a hint.
     */
    T move(Moves moves);

    /**
     * This should output the name of your tipster strategy.
     *
     * @return String
     */
    String getDisplayName();
}