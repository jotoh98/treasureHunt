package com.treasure.hunt.game;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
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

    private Thread beatThread;

    @Getter
    private volatile BooleanProperty beatThreadRunning = new SimpleBooleanProperty(false);
    /**
     * Contains the "gameHistory".
     */
    private ObservableList<Move> moves = FXCollections.observableArrayList();

    private GameEngine gameEngine;

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
                .getDeclaredConstructor(Searcher.class, Hider.class)
                .newInstance(newSearcher, newHider);

        // Do initial move
        moves.add(gameEngine.init(JTSUtils.createPoint(0, 0)));
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
     * @param excludeOverrideItems if true Geometry items that are set to be overridable only the last item is returned
     * @return The whole List of geometryItems of the gameHistory
     */
    public List<GeometryItem> getGeometryItems(Boolean excludeOverrideItems) {
        ArrayList<GeometryItem> geometryItems = moves.subList(0, viewIndex.get() + 1).stream()
                .flatMap(move -> move.getGeometryItems().stream())
                .collect(Collectors.toCollection(ArrayList::new));
        if (!excludeOverrideItems) {
            return geometryItems;
        }
        Map<GeometryType, List<GeometryItem>> itemsByType = geometryItems.stream()
                .collect(Collectors.groupingBy(GeometryItem::getGeometryType));
        List<GeometryItem> filterList = itemsByType.keySet()
                .stream()
                .flatMap(type -> {
                    List<GeometryItem> itemsOfType = itemsByType.get(type);
                    if (!type.isOverride()) {
                        return itemsOfType.stream();
                    } else {
                        return Stream.of(itemsOfType.get(itemsOfType.size() - 1));
                    }
                })
                .collect(Collectors.toList());
        return filterList;
    }

    /**
     * @return whether the game of the {@link GameEngine} is finished or not.
     */
    public boolean isGameFinished() {
        return gameEngine.isFinished();
    }

    /**
     * Delegate for game engine finished property
     *
     * @return finished property
     */
    public BooleanProperty getGameFinishedProperty() {
        return gameEngine.getFinished();
    }

    /**
     * @return true if the shown step is the most up to date one
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
     * @return true if the shown step is the first one
     */
    public boolean isFirstStepShown() {
        return stepBackwardImpossibleBinding().getValue();
    }

    /**
     * @param x position on the canvas
     * @param y position on the canvas
     * @return the nearest {@link GeometryItem} to x and y
     */
    public GeometryItem pickGeometryItem(double x, double y) {
        if (moves.size() < 1) {
            throw new IllegalStateException("moves list is empty!");
        }

        Point mouse = JTSUtils.GEOMETRY_FACTORY.createPoint(new Coordinate(x, y));

        GeometryItem nearestGeometryItem = moves.get(0).getGeometryItems().get(0);
        for (Move move : moves.subList(0, viewIndex.get() + 1)) {
            for (GeometryItem geometryItem : move.getGeometryItems()) {
                if (mouse.distance(geometryItem.getGeometry()) < mouse.distance(nearestGeometryItem.getGeometry())) {
                    nearestGeometryItem = geometryItem;
                }
            }
        }
        return nearestGeometryItem;
    }
}
