package com.treasure.hunt.service.settings;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Session {

    private double windowWidth = 960d;
    private double windowHeight = 720d;
    private double windowTop = 100;
    private double windowLeft = 100;
    private Class<? extends Searcher> searcher = null;
    private Class<? extends Hider> hider = null;
    private Class<? extends GameEngine> engine = null;
    private boolean fullscreen = false;

}
