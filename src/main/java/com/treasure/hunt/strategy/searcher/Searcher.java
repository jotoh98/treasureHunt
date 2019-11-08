package com.treasure.hunt.strategy.searcher;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import org.locationtech.jts.geom.Point;

public interface Searcher<T extends Hint> {

    /**
     * Use this to initialize your searcher.
     *
     * @param startPosition the searchers starting position
     * @param gameHistory   the {@link GameHistory}, the searcher dumps his plans.
     */
    void init(Point startPosition, GameHistory gameHistory);

    /**
     * Use this to dump the searchers thoughts.
     * For this, you could use void {@link GameHistory#dump(Product)}
     *
     * @param product the {@link Product} to commit to the {@link com.treasure.hunt.view.in_game.View}
     */
    void commitProduct(Product product);

    /**
     * Use this to perform a initial move, without a hint given.
     * This is for the case, the searcher starts. (as he does normally)
     *
     * @return Moves the {@link Moves} the searcher did
     */
    Moves move();

    /**
     * @param hint the hint, the {@link Hider} gave last.
     * @return Moves the {@link Moves}, the searcher chose.
     */
    Moves move(T hint);

    /**
     * @return Point the position where the player currently stands.
     */
    Point getLocation();
}
