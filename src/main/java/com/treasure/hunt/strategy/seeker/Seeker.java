package com.treasure.hunt.strategy.seeker;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
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
     * For this, you could use void dump(List<Product> list) from GameHistory.
     *
     * @param product the {@link Product} to commit to the {@link com.treasure.hunt.view.in_game.View}
     */
    void commitProduct(Product product);

    /**
     * Use this to perform a initial move,
     * without a hint given.
     * This is for the case, the Seeker starts.
     *
     * @return Moves the {@link Moves} the seeker did
     */
    Moves move();

    /**
     * @param hint the hint, the {@link com.treasure.hunt.strategy.tipster.Tipster} gave last.
     * @return Moves the {@link Moves} choosed.
     */
    Moves move(T hint);

    /**
     * @return String the name of your seeker strategy
     */
    String getDisplayName();

    /**
     * @return Point the position where the player currently stands.
     */
    Point getLocation();
}
