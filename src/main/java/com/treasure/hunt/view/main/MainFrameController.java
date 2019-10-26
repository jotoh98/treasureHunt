package com.treasure.hunt.view.main;


import com.treasure.hunt.game.Normal;
import com.treasure.hunt.strategy.tipster.implementations.RandomAngularHintStrategy;
import com.treasure.hunt.strategy.seeker.implemenations.StrategyFromPaper;
import com.treasure.hunt.view.in_game.implementatons.CanvasView;

public class MainFrameController {
    private static MainFrameController single_instance = null;
    private Normal normal;
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

        normal = new Normal(new StrategyFromPaper(),
                new RandomAngularHintStrategy(),
                new CanvasView(canvasController.getCanvas()));
        normal.start();
    }

    public void onNext() {
        normal.onNext();
    }
}
