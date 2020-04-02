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
    protected final Point initialSearcherPoint;
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
        this(searcher, hider, JTSUtils.createPoint(0, 0));
    }

    /**
     * The constructor.
     *
     * @param searcher             playing the game
     * @param hider                playing the game
     * @param initialSearcherPoint the initial Searcher {@link Point}.
     */
    public GameEngine(Searcher searcher, Hider hider, Point initialSearcherPoint) {
        this.searcher = searcher;
        this.hider = hider;
        this.initialSearcherPoint = initialSearcherPoint;
    }

    /**
     * initialize searcher and treasure positions.
     *
     * @return a {@link Turn}, since the initialization must be displayed.
     */
    public Turn init() {
        searcherPos = initialSearcherPoint;
        searcher.init(searcherPos);
        hider.init(searcherPos);

        treasurePos = hider.getTreasureLocation();
        if (treasurePos == null) {
            throw new IllegalArgumentException("hider: " + hider + " gave a treasure position which is null.");
        }

        // Check, whether treasure spawns in range of searcher
        if (searcherPos.distance(treasurePos) <= Searcher.SCANNING_DISTANCE) {
            finished = true;
        }

        return new Turn(
                null,
                new SearchPath(searcherPos),
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

        final Point searchPathStart = lastSearchPath == null ? searcherPos : lastSearchPath.getLastPoint();

        searcherMove();

        if (located(lastSearchPath)) {
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
        Hint newHint = hider.move(lastSearchPath);
        assert (newHint != null);
        verifyHint(newHint, treasurePos, lastSearchPath.getLastPoint());
        lastHint = newHint;
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
        assert (lastSearchPath != null);

        lastSearchPath.addPointToFront(searcherPos);

        assert (lastSearchPath.getPoints().size() != 0);

        searcherPos = lastSearchPath.getLastPoint();
    }

    /**
     * TODO implement:
     * AngleHints must be of angle [0, 180] !?
     * Verifies whether the {@link Hint} {@code hint} given by the {@link Hider} followed the rules.
     *
     * @param hint             {@link Hint} to be verified
     * @param treasurePosition treasure position
     * @param searcherPosition searcher position
     */
    protected void verifyHint(Hint hint, Point treasurePosition, Point searcherPosition) {
        if (hint instanceof AngleHint) {
            GeometryAngle geometryAngle = ((AngleHint) hint).getGeometryAngle();
            if (!geometryAngle.inView(treasurePosition.getCoordinate())) {
                throw new IllegalArgumentException("Treasure does not lie in given Angle.");
            }
            if (!JTSUtils.doubleEqual(geometryAngle.getCenter().distance(searcherPosition.getCoordinate()), 0)) {
                throw new IllegalArgumentException("Treasure does not originate in the searcher's last position.");
            }
        }
        if (hint instanceof CircleHint) {
            Circle lastCircleHint = ((CircleHint) hint).getCircle();
            Circle newCircleHint = ((CircleHint) hint).getCircle();
            // check, whether the CircleHint contains the treasure.
            if (!newCircleHint.inside(treasurePosition.getCoordinate())) {
                throw new IllegalArgumentException("The CircleHint does not contain the treasure.\n" +
                        "It says, " + newCircleHint.getRadius() + " around " + newCircleHint.getCenter() + ", " +
                        "but was " + newCircleHint.getCenter().distance(treasurePosition.getCoordinate()));
            }
            // check, whether the current CircleHint lies completely in the previous.
            if (lastHint != null) {
                if (lastCircleHint.getRadius() > (lastCircleHint.distance(newCircleHint) + newCircleHint.getRadius())) {
                    throw new IllegalArgumentException("New CircleHint does not completely lie in the last Circle Hint.");
                }
            }
        }
    }

    /**
     * @param searchPath the {@link SearchPath}, the {@link Searcher} moved.
     * @return {@code true}, if the {@link Searcher} found the treasure. {@code false}, otherwise.
     * The {@link Searcher} found the treasure, if had a distance of &le; {@link Searcher#SCANNING_DISTANCE} in this SearchPath.
     * @throws IllegalStateException if this SearchPath contains zero {@link Point}s.
     */
    public boolean located(SearchPath searchPath) {
        if (searchPath.getPoints().size() < 1) {
            throw new IllegalStateException("The SearchPath should never got zero points!");
        }

        if (searchPath.getPoints().size() == 1) {
            return searchPath.getPoints().get(0).distance(treasurePos) <= Searcher.SCANNING_DISTANCE;
        }

        return searchPath.getLines().stream()
                .map(line -> line.distance(treasurePos))
                .anyMatch(distance -> distance <= Searcher.SCANNING_DISTANCE);
    }
}
