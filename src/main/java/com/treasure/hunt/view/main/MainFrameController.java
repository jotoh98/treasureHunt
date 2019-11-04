package com.treasure.hunt.view.main;


import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.view.in_game.implementatons.CanvasView;

import java.util.Collections;
import java.util.List;

public class MainFrameController {
    private static MainFrameController single_instance = null;

    private MainFrameController() {
    }

    public static MainFrameController getInstance() {
        if (single_instance == null) {
            single_instance = new MainFrameController();
        }
        return single_instance;
    }

    public void start() {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.show();
    }

    public void onPlay(Class<? extends Searcher> searcherClass, Class<? extends Hider> hiderClass, Class<? extends GameManager> gameManager) throws Exception {
        Searcher newSearcher = searcherClass.getDeclaredConstructor().newInstance();
        Hider newHider = hiderClass.getDeclaredConstructor().newInstance();
        CanvasView canvasView = new CanvasView();
        gameManager
                .getDeclaredConstructor(Searcher.class, Hider.class, List.class)
                .newInstance(newSearcher, newHider, Collections.singletonList(canvasView));
        new CanvasController(canvasView);
    }
}
