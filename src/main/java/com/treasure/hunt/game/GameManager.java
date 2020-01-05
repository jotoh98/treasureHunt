package com.treasure.hunt.game;

import com.google.common.annotations.VisibleForTesting;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The GameManager stores every {@link Move}-objects, happened in the game,
 * the binds the views to update them for every move and
 * runs the GameEngine step for step.
 *
 * @author dorianreineccius
 */
@Slf4j
public class GameManager {
    /**
     * A thread that is invoked by {@link GameManager#beat(ReadOnlyObjectProperty)} and stopped by {@link GameManager#stopBeat()}.
     * He executes {@link GameManager#move(int)} in a given interval.
     */
    private Thread beatThread;

    @Getter
    private volatile BooleanProperty beatThreadRunning = new SimpleBooleanProperty(false);
    /**
     * Contains the "gameHistory".
     */
    @VisibleForTesting
    ObservableList<Move> moves = FXCollections.observableArrayList();

    private GameEngine gameEngine;
    private BooleanProperty finished = new SimpleBooleanProperty(false);
    @Getter
    private IntegerProperty viewIndex = new SimpleIntegerProperty(0);

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

        Searcher newSearcher = searcherClass.getDeclaredConstructor().newInstance();
        Hider newHider = hiderClass.getDeclaredConstructor().newInstance();

        this.gameEngine = gameEngineClass
                .getDeclaredConstructor(Searcher.class, Hider.class, Coordinate.class)
                .newInstance(newSearcher, newHider, new Coordinate(0, 0));

        // Do initial move
        moves.add(gameEngine.init());
        if (gameEngine.isFinished()) {
            finished.set(true);
        }
        viewIndex.set(0);
    }

    public void addListener(ListChangeListener<? super Move> listChangeListener) {
        moves.addListener(listChangeListener);
    }

    public ObjectBinding<Move> lastMove() {
        return Bindings.createObjectBinding(() -> moves.get(viewIndex.get()), viewIndex, moves);
    }

    public ObjectBinding<Point> lastTreasure() {
        return Bindings.createObjectBinding(() -> moves.get(viewIndex.get()).getTreasureLocation(), viewIndex, moves);
    }

    public ObjectBinding<Point> lastPoint() {
        return Bindings.createObjectBinding(() -> moves.get(viewIndex.get()).getMovement().getEndPoint(), viewIndex, moves);
    }

    /**
     * Works only for stepSim &le; stepView 
     */
    public void next() {
        if (viewIndex.get() < moves.size()) {
            if (latestStepViewed()) {
                moves.add(gameEngine.move());
            }
            viewIndex.set(viewIndex.get() + 1);
        }
        if (gameEngine.isFinished()) {
            finished.set(true);
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
            if (gameEngine.isFinished()) {
                break;
            }
            next();
        }
    }

    /**
     * Works only for stepView &gt; 0
     */
    public void previous() {
        if (viewIndex.get() > 0) {
            viewIndex.set(viewIndex.get() - 1);
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
     * This simulates the whole game, until its finished.
     *
     * @param delay time between each move
     */
    public void beat(ReadOnlyObjectProperty<Double> delay) {
        if (beatThreadRunning.get()) {
            log.warn("There's already a beating thread running");
            return;
        }

        beatThreadRunning.set(true);
        beatThread = new Thread(() -> {
            log.debug("Start beating thread");
            while (!stepForwardImpossibleBinding().get() && beatThreadRunning.get()) {
                CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    next();
                    latch.countDown();
                });
                try {
                    latch.await();
                    Thread.sleep((long) (delay.get() * 1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            log.debug("Terminating beating thread");
            Platform.runLater(() -> beatThreadRunning.set(false));
        });
        beatThread.setDaemon(true);
        beatThread.start();
    }

    /**
     * Stops the Thread from beating.
     */
    public void stopBeat() {
        log.debug("Stopping beating thread");
        beatThreadRunning.set(false);
    }

    /**
     * @param filterItems if true Geometry items that are set to be overridable only the last item is returned and later deleted items are removed
     * @return The whole List of geometryItems of the gameHistory
     */
    public List<GeometryItem> getGeometryItems(Boolean filterItems) {
        ArrayList<GeometryItem> geometryItems = moves.subList(0, viewIndex.get() + 1).stream()
                .flatMap(move -> move.getGeometryItems().stream())
                .collect(Collectors.toCollection(ArrayList::new));
        if (!filterItems) {
            return geometryItems;
        }
        Map<GeometryType, List<GeometryItem>> itemsByType = geometryItems.stream()
                .collect(Collectors.groupingBy(GeometryItem::getGeometryType));

        return itemsByType.keySet()
                .stream()
                .flatMap(type -> {
                    List<GeometryItem> itemsOfType = itemsByType.get(type);
                    if (!type.isOverride()) {
                        return itemsOfType.stream();
                    } else {
                        return Stream.of(itemsOfType.get(itemsOfType.size() - 1));
                    }
                })
                .filter(geometryItem -> moves.stream().noneMatch(move ->
                        move.getHint() != null && move.getHint().getToBeRemoved().contains(geometryItem) ||
                                move.getMovement() != null && move.getMovement().getToBeRemoved().contains(geometryItem)
                ))
                .collect(Collectors.toList());
    }

    /**
     * @return finished property containing {@code true}, if the game is over. {@code false}, otherwise.
     */
    public BooleanProperty getGameFinishedProperty() {
        return finished;
    }

    /**
     * @return {@code true}, if the shown step is the most up to date one. {@code false}, otherwise.
     */
    public boolean latestStepViewed() {
        return moves.size() - 1 == viewIndex.get();
    }

    public BooleanBinding latestStepViewedBinding() {
        return Bindings.createBooleanBinding(() -> moves.size() - 1 == viewIndex.get(), viewIndex, moves);
    }

    public BooleanBinding stepForwardImpossibleBinding() {
        return getGameFinishedProperty().and(latestStepViewedBinding());
    }

    public BooleanBinding stepBackwardImpossibleBinding() {
        return viewIndex.isEqualTo(0);
    }

    /**
     * @return {@code true}, if the shown step is the first one. {@code false}, otherwise.
     */
    public boolean isFirstStepShown() {
        return stepBackwardImpossibleBinding().getValue();
    }
}
