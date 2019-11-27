package com.treasure.hunt.view.main;


import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.view.in_game.impl.CanvasView;

import java.util.Collections;
import java.util.List;

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

    public CanvasController initGame(Class<? extends Searcher> searcherClass, Class<? extends Hider> hiderClass, Class<? extends GameManager> gameManager) throws Exception {
        Searcher newSearcher = searcherClass.getDeclaredConstructor().newInstance();
        Hider newHider = hiderClass.getDeclaredConstructor().newInstance();
        CanvasView canvasView = new CanvasView();
        GameManager gameManagerInstance = gameManager
                .getDeclaredConstructor(Searcher.class, Hider.class, List.class)
                .newInstance(newSearcher, newHider, Collections.singletonList(canvasView));
        return new CanvasController(canvasView, gameManagerInstance);
    }
}
