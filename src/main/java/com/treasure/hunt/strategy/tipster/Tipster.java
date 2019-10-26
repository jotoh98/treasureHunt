package com.treasure.hunt.strategy.tipster;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.hint.Product;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.seeker.Moves;
import org.locationtech.jts.geom.Point;

public interface Tipster<T extends Hint> {

    /**
     * Use this to initialize your Tipster.
     * This will give him the GameHistory.
     *
     * @param gameHistory
     */
    void init(GameHistory gameHistory);

    /**
     * This should let the Tipster commit all of his created GeometryItems
     * to the View-Thread.
     * For this, you could use void dump(List<Product> list) from GameHistory.
     */
    void commitProduct(Product product);

    /**
     * Use this to tell a hint,
     * only knowing the current position of the {@Seeker}.
     * This is for the case, the Tipster starts.
     *
     * @return T a hint.
     */
    T move(Point currentPos);

    /**
     * Use this to tell a hint,
     * knowing the last Moves of the {@Seeker}.
     *
     * @return T a hint.
     */
    T move(Moves moves);

    /**
     * This should output the name of your Tipster-Strategy.
     *
     * @return String
     */
    String getDisplayName();

    /**
     * This should output the current treasurelocation.
     *
     * @return Point
     */
    Point getTreasureLocation();
}