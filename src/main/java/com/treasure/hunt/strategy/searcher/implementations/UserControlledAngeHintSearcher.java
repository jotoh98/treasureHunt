package com.treasure.hunt.strategy.searcher.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.game.mods.hideandseek.HideAndSeekSearcher;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.util.JTSUtils;
import org.locationtech.jts.geom.Point;

public class UserControlledAngeHintSearcher implements HideAndSeekSearcher<Hint> {
    private Point startPosition;

    @Override
    public void init(Point startPosition, GameHistory gameHistory) {
        this.startPosition = startPosition;
    }

    @Override
    public Moves move() {
        Moves moves = new Moves();
        moves.addWayPoint(startPosition);
        return moves;
    }

    @Override
    public Moves move(Hint hint) {
        Point moveTo = JTSUtils.promptForPoint("Please provide a move location", "Hint is: " + hint);
        Moves moves = new Moves();
        moves.addWayPoint(moveTo);
        return moves;
    }


}
