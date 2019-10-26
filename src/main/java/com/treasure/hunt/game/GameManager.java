package com.treasure.hunt.game;

import com.treasure.hunt.strategy.seeker.Seeker;
import com.treasure.hunt.strategy.tipster.Tipster;
import com.treasure.hunt.view.in_game.View;

import java.util.List;

/**
 * Use this to implement different GameManagers
 * for implementing different game-modes.
 */
public interface GameManager {

    void init(Seeker seeker, Tipster tipster, List<View> view);
    void run();
    void next();
}
