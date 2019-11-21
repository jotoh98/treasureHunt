package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekSearcher;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.SwingUtils;
import org.locationtech.jts.geom.Point;

public class UserControlledAngleHintSearcher implements HideAndSeekSearcher<Hint> {
    private Point currentPosition;

    @Override
    public void init(Point startPosition) {
        currentPosition = startPosition;
    }

    @Override
    public Movement move() {
        Point moveTo = SwingUtils.promptForPoint("Please provide a initial move location", "");
        currentPosition = moveTo;
        Movement movement = new Movement(currentPosition);
        movement.addWayPoint(moveTo);
        return movement;
    }

    @Override
    public Movement move(Hint hint) {
        Point moveTo = SwingUtils.promptForPoint("Please provide a move location", "Hint is: " + hint);
        currentPosition = moveTo;
        Movement movement = new Movement(currentPosition);
        movement.addWayPoint(moveTo);
        return movement;
    }


}
