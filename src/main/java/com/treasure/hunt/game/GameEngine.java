package com.treasure.hunt.game;

import com.treasure.hunt.analysis.Statistic;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.SearchPathPrototype;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.ListUtils;
import com.treasure.hunt.utils.Requires;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the engine which runs a simulation of a treasure hunt.
 *
 * @author dorianreineccius
 */
@Requires(hider = Hider.class, searcher = Searcher.class)
public class GameEngine {
    @Getter
    protected final Searcher searcher;
    @Getter
    protected final Hider hider;
    protected final Coordinate initialSearcherCoordinate;
    /**
     * Tells, whether the game is done or not.
     */
    @Getter
    @Setter
    protected boolean finished = false;
    /**
     * Tells, whether a first move is happened in the game yet, or not.
     */
    protected boolean firstMove = true;
    protected Hint lastHint;
    protected SearchPath lastSearchPath;
    protected Coordinate searcherPos;
    protected Coordinate treasurePos;
    @Getter
    private final Statistic statistics = new Statistic();

    /**
     * The constructor.
     *
     * @param searcher playing the game
     * @param hider    playing the game
     */
    public GameEngine(Searcher searcher, Hider hider) {
        this(searcher, hider, new Coordinate(0, 0));
    }

    /**
     * The constructor.
     *
     * @param searcher                  playing the game
     * @param hider                     playing the game
     * @param initialSearcherCoordinate the initial Searcher {@link Coordinate}.
     */
    public GameEngine(Searcher searcher, Hider hider, Coordinate initialSearcherCoordinate) {
        this.searcher = searcher;
        this.hider = hider;
        this.initialSearcherCoordinate = initialSearcherCoordinate;
    }

    public static boolean located(List<Coordinate> coordinates, Coordinate treasure) {
        if (coordinates.isEmpty()) {
            throw new IllegalArgumentException("points must not be empty!");
        }

        if (coordinates.size() == 1) {
            return coordinates.get(0).distance(treasure) <= Searcher.SCANNING_DISTANCE;
        }

        List<Coordinate> wayCoordinates = coordinates.stream()
                .collect(Collectors.toList());

        return ListUtils
                .consecutive(wayCoordinates, (firstCoordinate, nextCoordinate) ->
                        Distance.pointToSegment(treasure, firstCoordinate, nextCoordinate)
                )
                .anyMatch(distance -> distance <= Searcher.SCANNING_DISTANCE);
    }

    /**
     * initialize searcher and treasure positions.
     *
     * @return a {@link Turn}, since the initialization must be displayed.
     */
    public Turn init() {
        searcherPos = initialSearcherCoordinate;
        searcher.init(JTSUtils.createPoint(searcherPos));
        hider.init(JTSUtils.createPoint(searcherPos));

        treasurePos = hider.getTreasureLocation().getCoordinate();
        if (treasurePos == null) {
            throw new IllegalArgumentException(hider + " gave a treasurePosition which is null.");
        }

        // Check, whether treasure spawns in range of searcher
        if (searcherPos.distance(treasurePos) <= Searcher.SCANNING_DISTANCE) {
            finished = true;
        }

        return new Turn(
                null,
                new SearchPath(new ArrayList<>(), searcherPos),
                treasurePos);
    }

    /**
     * This simulates just one step of the simulation.
     * The searcher begins since we want not force him,
     * to take a initial hint, he eventually do not need,
     * f.e. if he works randomized!
     *
     * @return the {@link Turn}, happened in this step.
     */
    public Turn move() {
        if (finished) {
            throw new IllegalStateException("Game is already finished");
        }

        searcherMove();

        if (located(lastSearchPath.getCoordinates(), treasurePos)) {
            finished = true;
            return new Turn(null, lastSearchPath, treasurePos);
        } else {
            hiderMove();
        }

        return new Turn(lastHint, lastSearchPath, treasurePos);
    }

    /**
     * Let the {@link GameEngine#hider} give its {@link Hint}.
     */
    protected void hiderMove() {
        lastHint = hider.move(lastSearchPath);
        assert (lastHint != null);
        verifyHint(lastHint, treasurePos);
    }

    /**
     * Let the {@link GameEngine#searcher} make {@link SearchPathPrototype}.
     */
    protected void searcherMove() {
        SearchPathPrototype tmpLastSearchPathPrototype;
        if (firstMove) {
            firstMove = false;
            tmpLastSearchPathPrototype = searcher.move();
        } else {
            tmpLastSearchPathPrototype = searcher.move(lastHint);
        }
        assert (tmpLastSearchPathPrototype != null);
        assert (tmpLastSearchPathPrototype.getCoordinates().size() != 0);

        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(searcherPos);
        coordinates.addAll(tmpLastSearchPathPrototype.getCoordinates());

        // build new SearchPath
        lastSearchPath = new SearchPath(tmpLastSearchPathPrototype.getAdditional().stream()
                .map(additionalGeometryItem -> additionalGeometryItem.clone())
                .collect(Collectors.toList()),
                coordinates);
        lastSearchPath.getGeometryItemsToBeRemoved().addAll(tmpLastSearchPathPrototype.getGeometryItemsToBeRemoved());
        lastSearchPath.getStatusMessageItemsToBeAdded().addAll(tmpLastSearchPathPrototype.getStatusMessageItemsToBeAdded());
        lastSearchPath.getStatusMessageItemsToBeRemoved().addAll(tmpLastSearchPathPrototype.getStatusMessageItemsToBeRemoved());

        searcherPos = lastSearchPath.getSearcherEndCoordinate();
    }

    /**
     * TODO implement:
     * AngleHints must be of angle [0, 180] !?
     * CircleHints must contain each other !?
     * Verifies whether the {@link Hint} {@code hint} given by the {@link Hider} followed the rules.
     *
     * @param hint             {@link Hint} to be verified
     * @param treasurePosition treasure position
     */
    protected void verifyHint(Hint hint, Coordinate treasurePosition) {
        if (hint instanceof AngleHint) {
            if (!((AngleHint) hint).getGeometry().inView(treasurePosition)) {
                throw new IllegalArgumentException("Treasure does not lie in given Angle.");
            }
        }
        if (hint instanceof CircleHint) {
            if (((CircleHint) hint).getRadius() < ((CircleHint) hint).getCenter().distance(treasurePosition)) {
                throw new IllegalArgumentException("The CircleHint does not contain the treasure.\n" +
                        "It says, " + ((CircleHint) hint).getRadius() + " around " + ((CircleHint) hint).getCenterPoint() + ", " +
                        "but was " + ((CircleHint) hint).getCenter().distance(treasurePosition));
            }
        }
    }
}
