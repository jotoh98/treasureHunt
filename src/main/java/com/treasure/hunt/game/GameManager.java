package com.treasure.hunt.game;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.view.in_game.View;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * The GameManager stores every {@link Move}-objects, happened in the game,
 * the {@link View} objects to run them for every move and
 * runs the GameEngine step for step.
 *
 * @author dorianreineccius
 */
public class GameManager {
    /**
     * The {@link View} objects (being {@link Runnable})
     */
    private List<Runnable> views = new ArrayList<>();
    /**
     * Runs the {@link View} objects concurrently.
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * Contains the "gameHistory".
     */
    private List<Move> moves = Collections.synchronizedList(new ArrayList<>());
    private GameEngine gameEngine;

    private int stepSim = 0;
    private int stepView = 0;

    /**
     * @param searcherClass   (Sub-)class of {@link Searcher}
     * @param hiderClass      (Sub-)class of {@link Hider}
     * @param gameEngineClass (Sub-)class of {@link GameEngine}
     * @param views           A list of {@link View} objects ({@link Runnable})
     * @throws NoSuchMethodException     from {@link Class#getDeclaredConstructor(Class[])}
     * @throws IllegalAccessException    from {@link java.lang.reflect.Constructor#newInstance(Object...)}
     * @throws InvocationTargetException from {@link java.lang.reflect.Constructor#newInstance(Object...)}
     * @throws InstantiationException    from {@link java.lang.reflect.Constructor#newInstance(Object...)}
     */
    public GameManager(Class<? extends Searcher> searcherClass, Class<? extends Hider> hiderClass, Class<? extends GameEngine> gameEngineClass, List<View> views)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Searcher newSearcher = searcherClass.getDeclaredConstructor().newInstance();
        Hider newHider = hiderClass.getDeclaredConstructor().newInstance();
        GameEngine gameEngineInstance = gameEngineClass
                .getDeclaredConstructor(Searcher.class, Hider.class)
                .newInstance(newSearcher, newHider);
        this.gameEngine = gameEngineInstance;

        // Register Views
        for (View view : views) {
            registerListener(view);
            view.init(this);
        }

        // Do initial move
        moves.add(gameEngine.init());
        stepView++;
        stepSim++;
        runListeners();
    }

    /**
     * Works only for stepSim <= stepView
     */
    public void next() {
        if (stepView <= stepSim) {

            if (stepView == stepSim) {
                moves.add(gameEngine.move());
                runListeners();
                stepSim++;
            }
            stepView++;
        }
        runListeners();
    }

    /**
     * This simulates the whole game, until its finished.
     */
    public void beat() {
        while (!gameEngine.isFinished()) {
            next();
        }
    }

    /**
     * Simulates a fixed number of moves.
     * Breaks, when the game is finished.
     *
     * @param steps number of steps
     */
    public void move(int steps) {
        for (int i = 0; i < steps; i++) {
            if (gameEngine.finished) {
                break;
            }
            next();
        }
    }

    /**
     * Works only for stepView > 0
     */
    public void previous() {
        if (stepView > 0) {
            stepView--;
        }
        runListeners();
    }

    /**
     * @param runnable the views, which gets registered.
     */
    public void registerListener(Runnable runnable) {
        views.add(runnable);
    }

    /**
     * This will run {@link Runnable#run()} of each {@link View}
     */
    public void runListeners() {
        views.forEach(runnable -> executorService.execute(runnable));
    }

    /**
     * @return The whole List of geometryItems of the gameHistory
     */
    public List<GeometryItem> getGeometryItems() {
        ArrayList<GeometryItem> geometryItems = moves.subList(0, stepView).stream()
                .flatMap(move -> move.getGeometryItems().stream()).collect(Collectors.toCollection(ArrayList::new));
        return geometryItems;
    }

    /**
     * @return whether the game of the {@link GameEngine} is finished or not.
     */
    public boolean isGameFinished() {
        return gameEngine.isFinished();
    }

    /**
     * @return true if the shown step is the most up to date one
     */
    public boolean isSimStepLatest() {
        return stepSim == stepView;
    }

    /**
     * @return true if the shown step is the first one
     */
    public boolean isFirstStepShown() {
        return stepView == 0;
    }
}
