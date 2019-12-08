package com.treasure.hunt.view.main;


import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.view.in_game.View;
import com.treasure.hunt.view.in_game.impl.CanvasView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author axel12
 */
public class MainFrameController {
    private static MainFrameController singleInstance = null;

    private MainFrameController() {
    }

    public static MainFrameController getInstance() {
        if (singleInstance == null) {
            singleInstance = new MainFrameController();
        }
        return singleInstance;
    }

    public void start() {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.show();
    }

    public void onPlay(Class<? extends Searcher> searcherClass, Class<? extends Hider> hiderClass, Class<? extends GameEngine> gameEngineClass) throws Exception {
        List<View> list = new ArrayList<>();
        CanvasView canvasView = new CanvasView();
        list.add(canvasView);
        GameManager gameManager = new GameManager(searcherClass, hiderClass, gameEngineClass);

        new CanvasController(canvasView, gameManager);
    }
}
