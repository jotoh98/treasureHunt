package com.treasure.hunt.game;

import com.treasure.hunt.analysis.Statistic;
import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.Requires;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

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
    @Getter
    private final Statistic statistics = new Statistic();
    /**
     * Tells, whether the game is done or not.
     */
    @Getter
    protected boolean finished = false;
    /**
     * Tells, whether a first move is happened in the game yet, or not.
     */
    protected boolean firstMove = true;
    protected Hint lastHint;
    protected SearchPath lastSearchPath;
    protected Point searcherPos;
    protected Point treasurePos;

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

    /**
     * @param searchPath a valid {@link SearchPath}, the {@link Searcher} moved.
     * @return {@code true}, if the {@link Searcher} found the treasure. {@code false}, otherwise.
     * The {@link Searcher} found the treasure, if had a distance of &le; {@link Searcher#SCANNING_DISTANCE} in this SearchPath.
     */
    public static boolean located(SearchPath searchPath, Point treasurePos) {
        if (searchPath.getPoints().size() == 1) {
            return searchPath.getPoints().get(0).distance(treasurePos) <= Searcher.SCANNING_DISTANCE;
        }
        return searchPath.getLines().stream()
                .map(line -> line.distance(treasurePos))
                .anyMatch(distance -> distance <= Searcher.SCANNING_DISTANCE);
    }

    /**
     * Initializes {@link Searcher}, {@link Hider} and the treasure position
     * and simulates an initial Step.
     *
     * @return a {@link Turn}, since the initialization must be displayed.
     */
    public Turn init() {
        searcherPos = JTSUtils.GEOMETRY_FACTORY.createPoint(initialSearcherCoordinate);
        searcher.init(searcherPos);
        hider.init(searcherPos);

        treasurePos = hider.getTreasureLocation();
        if (treasurePos == null) {
            throw new IllegalArgumentException("hider: " + hider + " gave a treasure position which is null.");
        }

        Turn initialTurn = new Turn(
                null,
                new SearchPath(searcherPos),
                treasurePos);

        verifySearchPath(initialTurn.getSearchPath());
        if (located(initialTurn.getSearchPath(), treasurePos)) {
            finished = true;
        }

        return initialTurn;
    }

    /**
     * Let the {@link GameEngine#hider} give its {@link Hint}.
     */
    protected void hiderMove() {
        Hint newHint = hider.move(lastSearchPath);
        verifyHint(newHint, treasurePos, lastSearchPath.getLastPoint());
        lastHint = newHint;
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

        if (located(lastSearchPath, treasurePos)) {
            finished = true;
            return new Turn(null, lastSearchPath, treasurePos);
        } else {
            hiderMove();
        }

        return new Turn(lastHint, lastSearchPath, treasurePos);
    }

    /**
     * Let the {@link GameEngine#searcher} make {@link SearchPath}.
     */
    protected void searcherMove() {
        if (firstMove) {
            firstMove = false;
            lastSearchPath = searcher.move();
        } else {
            lastSearchPath = searcher.move(lastHint);
        }
        lastSearchPath.addPointToFront(searcherPos);
        verifySearchPath(lastSearchPath);

        assert (lastSearchPath.getPoints().size() != 0);

        searcherPos = lastSearchPath.getLastPoint();
    }

    /**
     * Verifies whether the {@link SearchPath} {@code searchPath} given by the {@link Hider} followed the given rules.
     *
     * @param searchPath {@link Hint} to be verified
     * @throws IllegalArgumentException if the {@link SearchPath} {@code searchPath} did not followed the rules.
     */
    protected void verifySearchPath(SearchPath searchPath) {
        if (searchPath == null) {
            throw new IllegalArgumentException("Searcher " + searcher + " gave a SearchPath, which was null!");
        }
        if (searchPath.getPoints().size() < 1) {
            throw new IllegalStateException("The SearchPath should never got zero points!");
        }
    }

    /**
     * Verifies whether the {@link AngleHint} {@code angleHint} given by the {@link Hider} followed the given rules.
     *
     * @param angleHint        {@link AngleHint} to be verified
     * @param treasurePosition {@link Point} of the treasure position
     * @param searcherPosition {@link Point} of the searcher position
     * @throws IllegalArgumentException if the {@link AngleHint} {@code angleHint} did not followed the rules.
     */
    protected void verifyHint(AngleHint angleHint, Point treasurePosition, Point searcherPosition) {
        if (angleHint == null) {
            throw new IllegalArgumentException("Hider gave a Hint, which was null!");
        }
        if (!angleHint.getGeometryAngle().getCenter().equals(searcherPosition.getCoordinate())) {
            throw new IllegalArgumentException("AngleHint center do not lie on the player position.");
        }
        GeometryAngle geometryAngle = (angleHint).getGeometryAngle();
        if (!geometryAngle.inView(treasurePosition.getCoordinate())) {
            throw new IllegalArgumentException("Treasure does not lie in given Angle.");
        }
        if (!JTSUtils.doubleEqual(geometryAngle.getCenter().distance(searcherPosition.getCoordinate()), 0)) {
            throw new IllegalArgumentException("Treasure does not originate in the searcher's last position.");
        }

    }

    // TODO write tests for verifyHint!

    /**
     * Verifies whether the {@link CircleHint} {@code circleHint} given by the {@link Hider} followed the given rules.
     *
     * @param circleHint       {@link CircleHint} to be verified
     * @param treasurePosition {@link Point} of the treasure position
     * @param searcherPosition {@link Point} of the searcher position
     * @throws IllegalArgumentException if the {@link CircleHint} {@code circleHint} did not followed the rules.
     */
    protected void verifyHint(CircleHint circleHint, Point treasurePosition, Point searcherPosition) {
        if (circleHint == null) {
            throw new IllegalArgumentException("Hider gave a CircleHint, which was null!");
        }
        Circle lastCircleHint = (circleHint).getCircle();
        Circle newCircleHint = (circleHint).getCircle();
        if (!newCircleHint.inside(treasurePosition.getCoordinate())) {
            throw new IllegalArgumentException("The CircleHint does not contain the treasure.\n" +
                    "It says, " + newCircleHint.getRadius() + " around " + newCircleHint.getCenter() + ", " +
                    "but was " + newCircleHint.getCenter().distance(treasurePosition.getCoordinate()));
        }
        if (lastHint != null) {
            if (lastCircleHint.getRadius() > (lastCircleHint.distance(newCircleHint) + newCircleHint.getRadius())) {
                throw new IllegalArgumentException("New CircleHint does not completely lie in the last Circle Hint.");
            }
        }
    }

    /**
     * Verifies whether the {@link Hint} {@code hint} given by the {@link Hider} followed the given rules.
     *
     * @param hint             {@link Hint} to be verified
     * @param treasurePosition {@link Point} of the treasure position
     * @param searcherPosition {@link Point} of the searcher position
     * @throws IllegalArgumentException if the {@link Hint} {@code hint} did not followed the rules.
     */
    protected void verifyHint(Hint hint, Point treasurePosition, Point searcherPosition) {
        if (hint instanceof AngleHint) {
            verifyHint((AngleHint) hint, treasurePosition, searcherPosition);
        } else if (hint instanceof CircleHint) {
            verifyHint((CircleHint) hint, treasurePosition, searcherPosition);
        } else {
            throw new IllegalArgumentException("This type of hint is not known!");
        }
    }
}
