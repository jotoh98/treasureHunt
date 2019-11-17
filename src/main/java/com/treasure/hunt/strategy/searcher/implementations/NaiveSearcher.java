package com.treasure.hunt.strategy.searcher.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.game.mods.hideandseek.HideAndSeekSearcher;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Moves;
import org.locationtech.jts.geom.Point;

/**
 * The SpoiledSearcher follows an {@link com.treasure.hunt.strategy.hint.CircleHint} and
 * moves always in the center;
 */
public class NaiveSearcher implements HideAndSeekSearcher<Hint> {

    private GameHistory gameHistory;
    private Point position;

    @Override
    public void init(Point startPosition, GameHistory gameHistory) {
        position = startPosition;
        this.gameHistory = gameHistory;
    }

    @Override
    public void commitProduct(Product product) {
    }

    @Override
    public Moves move() {
        Moves moves = new Moves();
        moves.addWayPoint(position);
        gameHistory.dump(moves);
        return moves;
    }

    /**
     * Always go to the center of the hint.
     *
     * @param hint the hint, the {@link com.treasure.hunt.strategy.hider.Hider} gave last.
     * @return The {@link Moves}, the searcher did.
     */
    @Override
    public Moves move(Hint hint) {
        Moves moves = new Moves();
        moves.addWayPoint(position);
        moves.addWayPoint((Point) hint.getEndPoint().getObject());
        gameHistory.dump(moves);
        return moves;
    }

    @Override
    public Point getLocation() {
        return position;
    }
}
