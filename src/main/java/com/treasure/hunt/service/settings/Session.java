package com.treasure.hunt.service.settings;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Session {

    private double wWidth = 960d;
    private double wHeight = 720d;
    private double wTop = 100;
    private double wLeft = 100;
    private Class<? extends Searcher> searcher = null;
    private Class<? extends Hider> hider = null;
    private Class<? extends GameEngine> engine = null;
    private boolean fullscreen = false;

}
