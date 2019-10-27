package com.treasure.hunt.game.mods.insecurehints;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.view.in_game.View;

import java.util.List;

public class InsecureGameManager extends GameManager {

    protected InsecureSeeker seeker;
    protected InsecureTipster tipster;

    public InsecureGameManager(InsecureSeeker seeker, InsecureTipster tipster, List<View> view) {
        super(seeker, tipster, view);
    }
}
