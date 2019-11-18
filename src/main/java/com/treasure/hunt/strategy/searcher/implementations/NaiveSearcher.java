package com.treasure.hunt.strategy.searcher.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.game.mods.hideandseek.HideAndSeekSearcher;
import com.treasure.hunt.strategy.hint.CircleHint;
import com.treasure.hunt.strategy.searcher.Moves;
import org.locationtech.jts.geom.Point;

/**
 * The SpoiledSearcher follows an {@link com.treasure.hunt.strategy.hint.CircleHint} and
 * moves always in the center;
 */
public class NaiveSearcher implements HideAndSeekSearcher<CircleHint> {

    private GameHistory gameHistory;
    private Point position;

    @Override
    public void init(Point startPosition, GameHistory gameHistory) {
        position = startPosition;
        this.gameHistory = gameHistory;
    }

    @Override
    public Moves move() {
        Moves moves = new Moves();
        moves.addWayPoint(position);
        return moves;
    }

    /**
     * Always go to the center of the hint.
     *
     * @param circleHint the hint, the {@link com.treasure.hunt.strategy.hider.Hider} gave last.
     * @return The {@link Moves}, the searcher did.
     */
    @Override
    public Moves move(CircleHint circleHint) {
        Moves moves = new Moves();
        moves.addWayPoint(position);
        moves.addWayPoint(circleHint.getCenter());
        return moves;
    }

    @Override
    public Point getLocation() {
        return position;
    }
}
