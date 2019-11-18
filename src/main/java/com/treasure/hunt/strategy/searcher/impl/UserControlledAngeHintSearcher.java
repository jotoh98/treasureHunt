package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekSearcher;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

public class UserControlledAngeHintSearcher implements HideAndSeekSearcher<Hint> {
    private Point startPosition;

    @Override
    public void init(Point startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public Movement move() {
        Movement movement = new Movement();
        movement.addWayPoint(startPosition);
        return movement;
    }

    @Override
    public Movement move(Hint hint) {
        Point moveTo = JTSUtils.promptForPoint("Please provide a move location", "Hint is: " + hint);
        Movement movement = new Movement();
        movement.addWayPoint(moveTo);
        return movement;
    }


}
