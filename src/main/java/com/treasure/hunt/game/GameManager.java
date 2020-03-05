package com.treasure.hunt.game;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoCopyable;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.annotations.VisibleForTesting;
import com.treasure.hunt.analysis.StatisticObject;
import com.treasure.hunt.jts.geom.Shapeable;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.geom.StatusMessageItem;
import com.treasure.hunt.strategy.geom.StatusMessageType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.AsyncUtils;
import com.treasure.hunt.utils.JTSUtils;
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
import org.locationtech.jts.geom.Geometry;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The GameManager stores every {@link Turn}-objects, happened in the game,
 * the binds the views to update them for every move and
 * runs the GameEngine step for step.
 *
 * @author dorianreineccius
 */
@Slf4j
public class GameManager implements KryoSerializable, KryoCopyable<GameManager> {

    public static final double MOUSE_RECOGNIZE_DISTANCE = 0.2;
    private List<GeometryItem<?>> geometryItemsList = new ArrayList<>();
    private int geometryItemsListIndex = 0;
    private Coordinate lastMouseClick;
    /**
     * Contains the "gameHistory".
     */
    @VisibleForTesting
    @Getter
    ObservableList<Turn> turns = FXCollections.observableArrayList();
    @Getter
    private volatile BooleanProperty beatThreadRunning = new SimpleBooleanProperty(false);
    private GameEngine gameEngine;
    @Getter
    private BooleanProperty finishedProperty;
    @Getter
    private IntegerProperty viewIndex;
    @Getter
    private BooleanBinding latestStepViewedBinding;
    @Getter
    private ObjectBinding<Turn> lastMoveBinding;
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
        turns.add(gameEngine.init());
        if (gameEngine.isFinished()) {
            finishedProperty.set(true);
        }
    }

    private void setProperties() {
        viewIndex = new SimpleIntegerProperty(0);
        finishedProperty = new SimpleBooleanProperty(false);
    }

    private void setBindings() {
        latestStepViewedBinding = Bindings.createBooleanBinding(() -> turns.size() - 1 == viewIndex.get(), viewIndex, turns);
        stepForwardImpossibleBinding = finishedProperty.and(latestStepViewedBinding);
        statistics = Bindings.createObjectBinding(() -> gameEngine.getStatistics().calculate(getVisibleTurns()), viewIndex);
        stepBackwardImpossibleBinding = viewIndex.isEqualTo(0);
        lastMoveBinding = Bindings.createObjectBinding(() -> turns.get(viewIndex.get()), viewIndex, turns);
        lastTreasureBindings = Bindings.createObjectBinding(() -> turns.get(viewIndex.get()).getTreasureLocation(), viewIndex, turns);
        lastPointBinding = Bindings.createObjectBinding(() -> turns.get(viewIndex.get()).getSearchPath().getLastPoint(), viewIndex, turns);
        moveSizeBinding = Bindings.size(turns);
        statusMessageItemsBinding = Bindings.createObjectBinding(this::getStatusMessageItems, viewIndex);
    }

    @NotNull
    private List<StatusMessageItem> getStatusMessageItems() {
        Map<StatusMessageType, List<StatusMessageItem>> statusByType = getVisibleTurns().stream()
                .flatMap(turn -> Stream.of(turn.getHint(), turn.getSearchPath()))
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
                .filter(statusMessageItem -> turns.stream().noneMatch(turn ->
                        turn.getHint() != null && turn.getHint().getStatusMessageItemsToBeRemoved().contains(statusMessageItem) ||
                                turn.getSearchPath() != null && turn.getSearchPath().getStatusMessageItemsToBeRemoved().contains(statusMessageItem)
                ))
                .collect(Collectors.toList());
    }

    /**
     * Works only for stepSim &le; stepView 
     */
    public void next() {
        if (viewIndex.get() < turns.size()) {
            if (latestStepViewed()) {
                turns.add(gameEngine.move());
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
        int viewIndexSnapshot = viewIndex.get();
        if (viewIndexSnapshot > 0) {
            for (int i = viewIndexSnapshot; i < turns.size(); i++) {
                log.info("" + i + " of " + turns.size());
                if (i == 0) {
                    if (!turns.get(i).getGeometryItems(JTSUtils.createPoint(0, 0)).isEmpty()) {
                        turns.get(i).getGeometryItems(JTSUtils.createPoint(0, 0)).forEach(geometryItem -> {
                            geometryItem.setSelected(false);
                        });
                    }
                } else {
                    if (!turns.get(i).getGeometryItems(turns.get(i - 1).getSearchPath().getLastPoint()).isEmpty()) {
                        turns.get(i).getGeometryItems(turns.get(i - 1).getSearchPath().getLastPoint()).forEach(geometryItem -> {
                            geometryItem.setSelected(false);
                        });
                    }
                }
            }
            viewIndex.set(viewIndexSnapshot - 1);
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
        return turns.size() - 1 == viewIndex.get();
    }

    /**
     * @param excludeOverrideItems if true Geometry items that are set to be overridable only the last item is returned and later deleted items are removed
     * @return The whole List of geometryItems of the gameHistory
     */
    public List<GeometryItem<?>> getGeometryItems(Boolean excludeOverrideItems) {

        List<Turn> visible = getVisibleTurns();

        //TODO: move to #151-rendering-pipeline
        ArrayList<GeometryItem<?>> geometryItems = IntStream
                .range(0, visible.size())
                .boxed()
                .flatMap(index -> {
                    Point lastMove = null;
                    if (index > 0) {
                        lastMove = visible.get(index - 1).getSearchPath().getLastPoint();
                    }
                    return visible.get(index).getGeometryItems(lastMove).stream();
                })
                .collect(Collectors.toCollection(ArrayList::new));

        if (!excludeOverrideItems) {
            return geometryItems;
        }
        Map<GeometryType, List<GeometryItem<?>>> itemsByType = geometryItems.stream()
                .collect(Collectors.groupingBy(GeometryItem::getGeometryType));

        return itemsByType.keySet()
                .stream()
                .flatMap(type -> {
                    List<GeometryItem<?>> itemsOfType = itemsByType.get(type);
                    if (!type.isOverride()) {
                        return itemsOfType.stream();
                    } else {
                        return Stream.of(itemsOfType.get(itemsOfType.size() - 1));
                    }
                })
                .filter(geometryItem -> turns.stream().noneMatch(turn ->
                        turn.getHint() != null && turn.getHint().getGeometryItemsToBeRemoved().contains(geometryItem) ||
                                turn.getSearchPath() != null && turn.getSearchPath().getGeometryItemsToBeRemoved().contains(geometryItem)
                ))
                .collect(Collectors.toList());
    }

    /**
     * @return only viewed moves
     */
    public List<Turn> getVisibleTurns() {
        return turns.subList(0, viewIndex.get() + 1);
    }

    /**
     * {@code executeNextOnJavaFxThread} defaults to {@code true}.
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
                    try {
                        next();
                    } catch (Exception e) {
                        completableFuture.completeExceptionally(e);
                    }
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
        kryo.writeObject(output, new ArrayList<>(turns));
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
        turns = FXCollections.observableArrayList(kryo.readObject(input, ArrayList.class));
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
        gameManager.turns = FXCollections.observableArrayList(turns);
        gameManager.viewIndex = new SimpleIntegerProperty(viewIndex.get());
        gameManager.finishedProperty = new SimpleBooleanProperty(finishedProperty.get());
        gameManager.setBindings();
        return gameManager;
    }

    /**
     * @param coordinate the point on the canvas, we want to get the closest {@link GeometryType} to.
     * @param distance   the maximum distance to a potential {@link GeometryItem}.
     * @return a sorted list, containing the nearest {@link GeometryItem}'s to {@code coordinate}, with a maximum distance of {@code distance}.
     */
    private List<GeometryItem<?>> pickGeometryItem(Coordinate coordinate, double distance) {
        List<GeometryItem<?>> geometryItems = getGeometryItems(true);
        if (geometryItems.size() < 1) {
            return new ArrayList<>();
        }

        Point mouse = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinate);

        log.info("mouse: " + mouse.getCoordinate());

        geometryItems = geometryItems.stream()
                .filter(geometryItem -> geometryItem.getObject() instanceof Geometry || geometryItem.getObject() instanceof Shapeable) // TODO also allow non-geometries
                .filter(geometryItem ->
                        {
                            //log.info(/*"mouse: " + mouse + ", geometryItem: " + geometryItem.getObject() + */
                            //        ", distance: " + mouse.distance((Geometry) geometryItem.getObject()) + " / " + distance);
                            return mouse.distance((Geometry) geometryItem.getObject()) <= distance;
                        }
                )
                .filter(geometryItem -> geometryItem.getGeometryStyle().isVisible())
                .sorted((geometryItem, secondGeometryItem) ->
                        (int) (mouse.distance((Geometry) geometryItem.getObject()) -
                                mouse.distance((Geometry) secondGeometryItem.getObject()))
                )
                .collect(Collectors.toList());

        return geometryItems;
    }

    public void refreshHighlighter(Coordinate coordinate, double scale) {

        double distance = MOUSE_RECOGNIZE_DISTANCE / scale;

        // unselect all
        for (GeometryItem geometryItem : geometryItemsList) {
            geometryItem.setSelected(false);
        }

        if (lastMouseClick == null) {
            lastMouseClick = coordinate;
        }

        // new mouse coordinate
        if (lastMouseClick.getX() != coordinate.getX() ||
                lastMouseClick.getY() != coordinate.getY()) {

            geometryItemsListIndex = 0;
            geometryItemsList = pickGeometryItem(coordinate, distance);

            lastMouseClick = coordinate;

            if (geometryItemsList.size() < 1) {
                return;
            }
        } else { // same mouse coordinate
            if (geometryItemsList.size() < 1) {
                return;
            }
            geometryItemsListIndex = (geometryItemsListIndex + 1) % geometryItemsList.size();
        }
        geometryItemsList.get(geometryItemsListIndex).setSelected(true);

        log.info("received: " + (geometryItemsListIndex + 1) + "/" + geometryItemsList.size());
        log.info("selected: " + geometryItemsList.get(geometryItemsListIndex).getObject());
    }
}
