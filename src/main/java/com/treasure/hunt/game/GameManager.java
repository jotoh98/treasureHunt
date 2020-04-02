package com.treasure.hunt.game;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoCopyable;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.annotations.VisibleForTesting;
import com.treasure.hunt.analysis.StatisticObject;
import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.*;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.AsyncUtils;
import com.treasure.hunt.utils.GeometryPipeline;
import com.treasure.hunt.utils.JTSUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import sun.reflect.ReflectionFactory;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The GameManager stores every {@link Turn}-objects, happened in the game,
 * the binds the views to update them for every move and
 * runs the GameEngine step for step.
 *
 * @author dorianreineccius
 */
@Preference(name = PreferenceService.EARLY_EXIT_AMOUNT, value = 0)
@Preference(name = PreferenceService.EARLY_EXIT_RADIUS, value = 1.0)
@Slf4j
public class GameManager implements KryoSerializable, KryoCopyable<GameManager> {

    /**
     * Contains the "gameHistory".
     */
    @VisibleForTesting
    @Getter
    ObservableList<Turn> turns = FXCollections.observableArrayList();
    /**
     * Contains additional {@link GeometryItem}'s which does not belong to the strategies,
     * like the {@link com.treasure.hunt.jts.geom.Grid} or {@link com.treasure.hunt.strategy.geom.GeometryType#HIGHLIGHTER};
     */
    @Getter
    private ObservableMap<String, GeometryItem<?>> additional = FXCollections.observableHashMap();

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
        if (turns.size() == 0) {
            turns.add(gameEngine.init());
        }
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
        statistics = Bindings.createObjectBinding(() -> gameEngine.getStatistics().calculate(getVisibleTurns(), gameEngine.isFinished()), viewIndex);
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
                .filter(statusMessageItem -> getVisibleTurns().stream().noneMatch(turn ->
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
        if (earlyExit()) {
            finishedProperty.setValue(true);
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
    public CompletableFuture<Void> beat(Integer maxSteps) {
        return beat(new SimpleObjectProperty<>(0d), false, maxSteps);
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
     * @return only viewed moves
     */
    public List<Turn> getVisibleTurns() {
        return turns.subList(0, viewIndex.get() + 1);
    }

    /**
     * {@code executeNextOnJavaFxThread} defaults to {@code true}.
     *
     * @see GameManager#beat(ReadOnlyObjectProperty, Boolean, Integer)
     */
    public CompletableFuture<Void> beat(ReadOnlyObjectProperty<Double> delay) {
        return beat(delay, true, null);
    }

    /**
     * This simulates the whole game, until its finished.
     *
     * @param delay                     time between each move
     * @param executeNextOnJavaFxThread if set to true the next call is made on javafx thread that is important when UI is attached to the GameManager,
     *                                  if it false the delay parameter is ignored
     */
    public CompletableFuture<Void> beat(ReadOnlyObjectProperty<Double> delay, Boolean executeNextOnJavaFxThread, Integer maxSteps) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        if (beatThreadRunning.get()) {
            log.warn("There's already a beating thread running");
            completableFuture.completeExceptionally(new IllegalStateException("There's already a beating thread running"));
            return completableFuture;
        }

        beatThreadRunning.set(true);
        AsyncUtils.EXECUTOR_SERVICE.submit(() -> {
            log.trace("Start beating thread");
            int steps = 1;
            while (!stepForwardImpossibleBinding.get() && beatThreadRunning.get() && (maxSteps == null || steps <= maxSteps)) {
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
                steps++;
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
        kryo.writeObject(output, new HashMap<>(additional));
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
        HashMap hashMap = kryo.readObject(input, HashMap.class);
        additional = FXCollections.observableHashMap();
        additional.putAll(hashMap);
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
        gameManager.additional = FXCollections.observableHashMap();
        gameManager.additional.putAll(additional);
        gameManager.setBindings();
        return gameManager;
    }


    /**
     * Add an additional {@link GeometryItem} to the rendering queue.
     *
     * @param key  name of the additional item
     * @param item the additional item
     */
    public void addAdditional(String key, GeometryItem<?> item) {
        additional.put(key, item);
    }

    /**
     * Remove an additional {@link GeometryItem} from the rendering queue.
     *
     * @param key name of the additional item to be removed
     */
    public void removeAdditional(String key) {
        additional.remove(key);
    }

    /**
     * Get visible geometry items.
     * The visible {@link Turn}s determine which {@link GeometryItem} are visible.
     *
     * @return stream of visible geometry items
     */
    public Stream<GeometryItem<?>> getVisibleGeometries() {
        List<GeometryItem<?>> subListGeometries = new ArrayList<>();

        subListGeometries.add(new GeometryItem<>(turns.get(0).getSearchPath().getFirstPoint(), GeometryType.WAY_POINT));

        turns.subList(0, viewIndex.get() + 1)
                .forEach(element -> subListGeometries.addAll(element.getGeometryItems()));

        final Stream<GeometryItem<?>> items = Stream.concat(subListGeometries.stream(), additional.values().stream());

        return GeometryPipeline.pipe(items);
    }

    /**
     * Whether the game exits early because of the search being stuck in a specified circular area.
     *
     * @return whether the game exits early or not
     */
    protected boolean earlyExit() {
        final double radius = PreferenceService.getInstance()
                .getPreference(PreferenceService.EARLY_EXIT_RADIUS, 1.0)
                .doubleValue();
        final int amount = PreferenceService.getInstance()
                .getPreference(PreferenceService.EARLY_EXIT_AMOUNT, 0)
                .intValue();

        if (turns.size() < 1 || amount < 2 || amount > turns.size() - 1) {
            return false;
        }

        final int toIndex = turns.size() - 1;
        final int fromIndex = Math.max(0, turns.size() - 1 - amount);

        final List<Turn> turnList = this.turns.subList(fromIndex, toIndex);

        final Coordinate origin = turnList.get(amount / 2).getSearchPath().getLastPoint().getCoordinate();

        final boolean isEarlyExit = turnList.stream()
                .map(Turn::getSearchPath)
                .map(SearchPath::getLastPoint)
                .map(Point::getCoordinate)
                .allMatch(coordinate -> origin.distance(coordinate) < radius);

        if (isEarlyExit) {
            turns.get(turns.size() - 1).getSearchPath().addAdditionalItem(new GeometryItem<>(new Circle(origin, radius), GeometryType.BOUNDING_CIRCE));
            turns.get(turns.size() - 1).getSearchPath().addAdditionalItem(new GeometryItem<>(JTSUtils.createPoint(origin), GeometryType.NO_TREASURE, new GeometryStyle(true, Color.CYAN, Color.red)));
        }
        return isEarlyExit;
    }
}
