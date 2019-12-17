package com.treasure.hunt.game;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.locationtech.jts.geom.Point;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The GameManager stores every {@link Move}-objects, happened in the game,
 * the binds the views to update them for every move and
 * runs the GameEngine step for step.
 *
 * @author dorianreineccius
 */
public class GameManager {

    /**
     * The gameEngine to simulate the game on.
     */
    private Searcher searcher;
    private Hider hider;
    private GameEngine gameEngine;

    /**
     * Contains the "gameHistory".
     */
    private ObservableList<Move> moves = FXCollections.observableArrayList();
    /**
     * The properties, to view the current game state.
     */
    private IntegerProperty stepSim = new SimpleIntegerProperty(0);
    private IntegerProperty stepView = new SimpleIntegerProperty(0);

    /**
     * @param searcherClass   (Sub-)class of {@link Searcher}
     * @param hiderClass      (Sub-)class of {@link Hider}
     * @param gameEngineClass (Sub-)class of {@link GameEngine}
     * @throws NoSuchMethodException     from {@link Class#getDeclaredConstructor(Class[])}
     * @throws IllegalAccessException    from {@link java.lang.reflect.Constructor#newInstance(Object...)}
     * @throws InvocationTargetException from {@link java.lang.reflect.Constructor#newInstance(Object...)}
     * @throws InstantiationException    from {@link java.lang.reflect.Constructor#newInstance(Object...)}
     */
    public GameManager(Class<? extends Searcher> searcherClass, Class<? extends Hider> hiderClass, Class<? extends GameEngine> gameEngineClass)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.searcher = searcherClass.getDeclaredConstructor().newInstance();
        this.hider = hiderClass.getDeclaredConstructor().newInstance();
        this.gameEngine = gameEngineClass
                .getDeclaredConstructor(Searcher.class, Hider.class)
                .newInstance(this.searcher, this.hider);

        // initialize/reset gameEngine
        reset();
    }

    public void addListener(ListChangeListener<? super Move> listChangeListener) {
        moves.addListener(listChangeListener);
    }

    public ObjectBinding<Move> lastMove() {
        return Bindings.createObjectBinding(() -> moves.get(stepView.get()), stepView, moves);
    }

    public ObjectBinding<Point> lastTreasure() {
        return Bindings.createObjectBinding(() -> moves.get(stepView.get()).getTreasureLocation(), stepView, moves);
    }

    public ObjectBinding<Point> lastPoint() {
        return Bindings.createObjectBinding(() -> moves.get(stepView.get()).getMovement().getEndPoint(), stepView, moves);
    }

    /**
     * Works only for stepSim <= stepView 
     */
    public void next() {
        if (stepView.get() <= stepSim.get()) {
            if (stepView.get() == stepSim.get()) {
                moves.add(gameEngine.move());
                stepSim.set(stepSim.get() + 1);
            }
            stepView.set(stepView.get() + 1);
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
        if (stepView.get() > 0) {
            stepView.set(stepView.get() - 1);
        }
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
     * @return The whole List of geometryItems of the gameHistory
     */
    public List<GeometryItem> getGeometryItems() {
        return moves.subList(0, stepView.get() + 1).stream()
                .flatMap(move -> move.getGeometryItems().stream())
                .collect(Collectors.toCollection(ArrayList::new));
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
        return stepSim.get() == stepView.get();
    }

    /**
     * @return true if the shown step is the first one
     */
    public boolean isFirstStepShown() {
        return stepView.isEqualTo(0).getValue();
    }

    /**
     * This reset the {@link GameEngine} and the corresponding {@link Searcher} and {@link Hider}.
     */
    public void reset() {
        // reset
        stepView.set(0);
        stepSim.set(0);
        moves.clear();

        // reset gameEngine
        gameEngine.reset();

        // Do initial move
        moves.add(gameEngine.init());
    }
}
