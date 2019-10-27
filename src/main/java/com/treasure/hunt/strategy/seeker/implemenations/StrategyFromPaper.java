package com.treasure.hunt.strategy.seeker.implemenations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.seeker.Moves;
import com.treasure.hunt.strategy.seeker.Seeker;
import org.locationtech.jts.geom.Point;

public class StrategyFromPaper implements Seeker<AngleHint> {

    @Override
    public void init(Point startPosition, GameHistory gameHistory) {

    }

    @Override
    public void commitProduct(Product product) {

    }

    @Override
    public Moves move() {
        return null;
    }

    @Override
    public Moves move(AngleHint hint) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Strategy from paper";
    }

    @Override
    public Point getLocation() {
        return null;
    }
}
