package com.treasure.hunt.view.in_game.impl;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.game.Move;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.view.in_game.View;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ConsoleOutputView implements View {

    private int i = 0;
    private GameHistory gameHistory;

    public void draw(GeometryItem geometryItem) {
        log.info(geometryItem.toString());
    }

    @Override
    public void run() {
        List<Move> moves;
        moves = gameHistory.giveNewProducts(i);
        moves.forEach(move -> move.getGeometryItems().forEach(geometryItem -> draw(geometryItem)));
        i += moves.size();
    }

    @Override
    public void init(GameHistory gameHistory) {
        this.gameHistory = gameHistory;
    }
}
