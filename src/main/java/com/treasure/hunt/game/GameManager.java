package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hint.generators.AbstractHintGenerator;
import com.treasure.hunt.strategy.search.AbstractSearchStrategy;
import com.treasure.hunt.ui.in_game.UiRenderer;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;

@RequiredArgsConstructor
public class GameManager {
    private final AbstractHintGenerator hintGenerator;
    private final AbstractSearchStrategy searchStrategy;
    private final UiRenderer uiRenderer;
    private final GameHistory gameHistory = new GameHistory();

    private Point player;
    private Point target;

    public void onNext() {

    }

    public void start() {

    }
}
