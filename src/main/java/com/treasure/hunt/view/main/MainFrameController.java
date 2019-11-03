package com.treasure.hunt.view.main;


import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.implementations.RandomAngularHintStrategy;
import com.treasure.hunt.strategy.searcher.implementations.StrategyFromPaper;
import com.treasure.hunt.view.in_game.View;
import com.treasure.hunt.view.in_game.implementatons.CanvasView;

import java.util.ArrayList;
import java.util.List;

public class MainFrameController {
    private static MainFrameController single_instance = null;
    private GameManager gameManager;
    private CanvasController canvasController;

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

    public void onPlay() {
        CanvasView canvasView = new CanvasView();

        List<View> viewList = new ArrayList<View>();
        viewList.add(canvasView);

        gameManager = new GameManager(new StrategyFromPaper(),
                new RandomAngularHintStrategy(),
                viewList);
        gameManager.run();
    }

    public void onNext() {
        gameManager.step();
    }
}
