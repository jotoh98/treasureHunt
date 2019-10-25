package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hint.Tipster;
import com.treasure.hunt.strategy.moves.Seeker;
import com.treasure.hunt.view.in_game.View;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;

@RequiredArgsConstructor
public class GameManager {
    private final Tipster hintGenerator;
    private final Seeker searchStrategy;
    private final View view;
    private final GameHistory gameHistory = new GameHistory();

    private Point player;
    private Point target;

    public void onNext() {

    }

    public void start() {
        Thread thread = new Thread(view, "View");
        thread.start(); // This starts the game thread.

    }
}
