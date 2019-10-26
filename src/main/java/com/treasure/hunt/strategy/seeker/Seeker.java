package com.treasure.hunt.strategy.seeker;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.hint.Product;
import com.treasure.hunt.strategy.hint.Hint;
import org.locationtech.jts.geom.Point;

public interface Seeker<T extends Hint> {

    /**
     * Use this to initialize your Seeker.
     * This will assign him a starting position and give him the GameHistory.
     *
     * @param startPosition
     * @param gameHistory
     */
    void init(Point startPosition, GameHistory gameHistory);

    /**
     * This should let the Seeker commit all of his created GeometryItems
     * to the View-Thread.
     * For this, you could use void dump(List<Product> list) from GameHistory.
     */
    void commitProduct(Product product);

    /**
     * Use this to perform a initial move,
     * without a hint given.
     * This is for the case, the Seeker starts.
     *
     * @return Moves
     */
    Moves move();

    /**
     * Use this to perform a move,
     * with a hint given.
     *
     * @param moves
     * @return Moves
     */
    Moves move(T moves);

    /**
     * This should output the name of your Tipster-Strategy.
     *
     * @return String
     */
    String getDisplayName();

    /**
     * This should output the current location of the Seeker.
     *
     * @return Point
     */
    Point getLocation();
}
