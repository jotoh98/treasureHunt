package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hint.AbstractHintGenerator;
import com.treasure.hunt.strategy.moves.AbstractMovesGenerator;
import com.treasure.hunt.ui.in_game.UiRenderer;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;

@RequiredArgsConstructor
public class GameManager {
    private final AbstractHintGenerator hintGenerator;
    private final AbstractMovesGenerator searchStrategy;
    private final UiRenderer uiRenderer;
    private final GameHistory gameHistory = new GameHistory();

    private Point player;
    private Point target;

    public void onNext() {

    }

    public void start() {

    }
}
