package com.treasure.hunt.game;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoCopyable;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.annotations.VisibleForTesting;
import com.treasure.hunt.analysis.StatisticObject;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.geom.StatusMessageItem;
import com.treasure.hunt.strategy.geom.StatusMessageType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.AsyncUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
public class GameManager implements KryoSerializable, KryoCopyable<GameManager> {

    @Getter
    private volatile BooleanProperty beatThreadRunning = new SimpleBooleanProperty(false);
    /**
     * Contains the "gameHistory".
     */
    @VisibleForTesting
    ObservableList<Move> moves = FXCollections.observableArrayList();

    private GameEngine gameEngine;
    @Getter
    private BooleanProperty finishedProperty;
    @Getter
    private IntegerProperty viewIndex;
    @Getter
    private BooleanBinding latestStepViewedBinding;
    @Getter
    private ObjectBinding<Move> lastMoveBinding;
    @Getter
    private ObjectBinding<Point> lastTreasureBindings;
    @Getter
    private ObjectBinding<Point> lastPointBinding;
    @Getter
    private IntegerBinding moveSizeBinding;
    @Getter
    private BooleanBinding stepForwardImpossibleBinding;
    @Getter
    private BooleanBinding stepBackwardImpossibleBinding;
    @Getter
    private ObjectBinding<List<StatisticObject>> statistics;
    @Getter
    private ObjectBinding<List<StatusMessageItem>> statusMessageItemsBinding;


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

        setProperties();
        setBindings();
    }

    public void init() {
        // Do initial move
        moves.add(gameEngine.init());
        if (gameEngine.isFinished()) {
            finishedProperty.set(true);
        }
    }

    private void setProperties() {
        viewIndex = new SimpleIntegerProperty(0);
        finishedProperty = new SimpleBooleanProperty(false);
    }

    private void setBindings() {
        latestStepViewedBinding = Bindings.createBooleanBinding(() -> moves.size() - 1 == viewIndex.get(), viewIndex, moves);
        stepForwardImpossibleBinding = finishedProperty.and(latestStepViewedBinding);
        statistics = Bindings.createObjectBinding(() -> gameEngine.getStatistics().calculate(getMovesViewed()), viewIndex);
        stepBackwardImpossibleBinding = viewIndex.isEqualTo(0);
        lastMoveBinding = Bindings.createObjectBinding(() -> moves.get(viewIndex.get()), viewIndex, moves);
        lastTreasureBindings = Bindings.createObjectBinding(() -> moves.get(viewIndex.get()).getTreasureLocation(), viewIndex, moves);
        lastPointBinding = Bindings.createObjectBinding(() -> moves.get(viewIndex.get()).getMovement().getEndPoint(), viewIndex, moves);
        moveSizeBinding = Bindings.size(moves);
        statusMessageItemsBinding = Bindings.createObjectBinding(this::getStatusMessageItems, moves);
    }

    @NotNull
    private List<StatusMessageItem> getStatusMessageItems() {
        Map<StatusMessageType, List<StatusMessageItem>> statusByType = moves.stream()
                .flatMap(move -> Stream.of(move.getHint(), move.getMovement()))
                .flatMap(hintAndMovement -> hintAndMovement == null ? Stream.empty() : hintAndMovement.getStatusMessageItemsToBeAdded().stream())
                .collect(Collectors.groupingBy(StatusMessageItem::getStatusMessageType));

        return statusByType.keySet()
                .stream()
                .flatMap(type -> {
                    List<StatusMessageItem> itemsOfType = statusByType.get(type);
                    if (!type.isOverride()) {
                        return itemsOfType.stream();
                    } else {
                        return Stream.of(itemsOfType.get(itemsOfType.size() - 1));
                    }
                })
                .filter(statusMessageItem -> moves.stream().noneMatch(move ->
                        move.getHint() != null && move.getHint().getStatusMessageItemsToBeRemoved().contains(statusMessageItem) ||
                                move.getMovement() != null && move.getMovement().getStatusMessageItemsToBeRemoved().contains(statusMessageItem)
                ))
                .collect(Collectors.toList());
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
            finishedProperty.set(true);
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
    public CompletableFuture<Void> beat() {
        return beat(new SimpleObjectProperty<>(0d), false);
    }

    /**
     * Stops the Thread from beating.
     */
    public void stopBeat() {
        log.debug("Stopping beating thread");
        beatThreadRunning.set(false);
    }

    /**
     * @return {@code true}, if the shown step is the most up to date one. {@code false}, otherwise.
     */
    public boolean latestStepViewed() {
        return moves.size() - 1 == viewIndex.get();
    }

    /**
     * @param excludeOverrideItems if true Geometry items that are set to be overridable only the last item is returned and later deleted items are removed
     * @return The whole List of geometryItems of the gameHistory
     */
    public List<GeometryItem> getGeometryItems(Boolean excludeOverrideItems) {
        ArrayList<GeometryItem> geometryItems = getMovesViewed().stream()
                .flatMap(move -> move.getGeometryItems().stream())
                .collect(Collectors.toCollection(ArrayList::new));
        if (!excludeOverrideItems) {
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
                        move.getHint() != null && move.getHint().getGeometryItemsToBeRemoved().contains(geometryItem) ||
                                move.getMovement() != null && move.getMovement().getGeometryItemsToBeRemoved().contains(geometryItem)
                ))
                .collect(Collectors.toList());
    }

    /**
     * @return only viewed moves
     */
    private List<Move> getMovesViewed() {
        return moves.subList(0, viewIndex.get() + 1);
    }

    /**
     * {@code executeNextOnJavaFxThread} defaults to {@link true}.
     *
     * @see GameManager#beat(ReadOnlyObjectProperty, Boolean)
     */
    public CompletableFuture<Void> beat(ReadOnlyObjectProperty<Double> delay) {
        return beat(delay, true);
    }

    /**
     * This simulates the whole game, until its finished.
     *
     * @param delay                     time between each move
     * @param executeNextOnJavaFxThread if set to true the next call is made on javafx thread that is important when UI is attached to the GameManager,
     *                                  if it false the delay parameter is ignored
     */
    public CompletableFuture<Void> beat(ReadOnlyObjectProperty<Double> delay, Boolean executeNextOnJavaFxThread) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        if (beatThreadRunning.get()) {
            log.warn("There's already a beating thread running");
            completableFuture.completeExceptionally(new IllegalStateException("There's already a beating thread running"));
            return completableFuture;
        }

        beatThreadRunning.set(true);
        AsyncUtils.EXECUTOR_SERVICE.submit(() -> {
            log.trace("Start beating thread");
            while (!stepForwardImpossibleBinding.get() && beatThreadRunning.get()) {
                if (executeNextOnJavaFxThread) {
                    CountDownLatch latch = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        next();
                        latch.countDown();
                    });
                    try {
                        latch.await();
                        Thread.sleep((long) (delay.get() * 1000));
                    } catch (InterruptedException e) {
                        completableFuture.completeExceptionally(e);
                        throw new RuntimeException(e);
                    }
                } else {
                    next();
                }
            }
            log.trace("Terminating beating thread");
            if (executeNextOnJavaFxThread) {
                Platform.runLater(() -> beatThreadRunning.set(false));
            } else {
                beatThreadRunning.set(false);
            }
            completableFuture.complete(null);
        });

        return completableFuture;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObject(output, gameEngine);
        output.writeBoolean(beatThreadRunning.get());
        kryo.writeObject(output, new ArrayList<>(moves));
        output.writeInt(viewIndex.get());
        output.writeBoolean(finishedProperty.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(Kryo kryo, Input input) {
        gameEngine = kryo.readObject(input, GameEngine.class);
        beatThreadRunning = new SimpleBooleanProperty(input.readBoolean());
        moves = FXCollections.observableArrayList(kryo.readObject(input, ArrayList.class));
        viewIndex = new SimpleIntegerProperty(input.readInt());
        finishedProperty = new SimpleBooleanProperty(input.readBoolean());
        setBindings();
    }

    public Class<? extends Searcher> getSearcherClass() {
        return gameEngine.getSearcher().getClass();
    }

    public Class<? extends Hider> getHiderClass() {
        return gameEngine.getHider().getClass();
    }

    public Class<? extends GameEngine> getGameEngineClass() {
        return gameEngine.getClass();
    }

    @SneakyThrows
    @Override
    public GameManager copy(Kryo kryo) {
        ReflectionFactory rf =
                ReflectionFactory.getReflectionFactory();
        Constructor objDef = Object.class.getDeclaredConstructor();
        Constructor intConstr = rf.newConstructorForSerialization(
                getClass(), objDef
        );
        GameManager gameManager = (GameManager) intConstr.newInstance();
        gameManager.gameEngine = kryo.copy(gameEngine);
        gameManager.beatThreadRunning = new SimpleBooleanProperty(beatThreadRunning.get());
        gameManager.moves = FXCollections.observableArrayList(moves);
        gameManager.viewIndex = new SimpleIntegerProperty(viewIndex.get());
        gameManager.finishedProperty = new SimpleBooleanProperty(finishedProperty.get());
        gameManager.setBindings();
        return gameManager;
    }
}
