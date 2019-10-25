package com.treasure.hunt.view.main;


import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hint.implementations.RandomAngularHintStrategy;
import com.treasure.hunt.strategy.moves.implemenations.StrategyFromPaper;
import com.treasure.hunt.view.in_game.implementatons.CanvasView;

public class MainFrameController {
    private static MainFrameController single_instance = null;
    private GameManager gameManager;
    private CanvasController canvasController;

    private MainFrameController() {
    }

    public static MainFrameController getInstance() {
        if (single_instance == null)
            single_instance = new MainFrameController();
        return single_instance;
    }

    public void start() {
        MainMenuController mainMenuController = new MainMenuController();
    }

    public void onPlay() {
        canvasController = new CanvasController();

        gameManager = new GameManager(new RandomAngularHintStrategy(), new StrategyFromPaper(), new CanvasView(canvasController.getCanvas()));
        gameManager.start();
    }

    public void onNext() {
        gameManager.onNext();
    }
}
