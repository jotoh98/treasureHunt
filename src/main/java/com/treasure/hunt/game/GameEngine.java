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
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the engine which runs a simulation of a treasure hunt.
 *
 * @author dorianreineccius
 */
@Requires(hider = Hider.class, searcher = Searcher.class)
public class GameEngine {
    public static double SCANNING_DISTANCE = 1.0;

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
     * @param searchPath a valid {@link SearchPath}, the {@link Searcher} moved.
     * @return {@code true}, if the {@link Searcher} found the treasure. {@code false}, otherwise.
     * The {@link Searcher} found the treasure, if had a distance of &le; {@link GameEngine#SCANNING_DISTANCE} in this SearchPath.
     */
    public static boolean located(SearchPath searchPath, Point treasurePos) {
        if (searchPath.getPoints().size() == 1) {
            return searchPath.getPoints().get(0).distance(treasurePos) <= SCANNING_DISTANCE;
        }
        return searchPath.getLines().stream()
                .map(line -> line.distance(treasurePos))
                .anyMatch(distance -> distance <= SCANNING_DISTANCE);
    }

    /**
     * @param searchPath  the {@link SearchPath}, in which the {@link Searcher} found the treasure.
     * @param treasurePos the {@link Point} the treasure lies on.
     * @return a cut {@link SearchPath}, containing only the points needed, to find the treasure.
     */
    public static SearchPath cutSearchPath(SearchPath searchPath, Point treasurePos) {
        if (searchPath.getPoints().size() == 1) {
            return searchPath;
        }
        for (int i = 0; searchPath.getLines().size() > i; i++) {
            if (searchPath.getLines().get(i).distance(treasurePos) <= SCANNING_DISTANCE) {
                SearchPath cutSearchPath = new SearchPath();
                /**
                 * This command takes the points 0,..,i since i + 1 is exclusive.
                 * The point i + 1 will added after.
                 */
                cutSearchPath.setPoints(new ArrayList<>(searchPath.getPoints().subList(0, i + 1)));
                List<Point> treasureIntersections = JTSUtils.circleLineIntersectionPoints(
                        searchPath.getPoints().get(i), searchPath.getPoints().get(i + 1), treasurePos, SCANNING_DISTANCE);
                // pick the point, closer to the i't points of the SearchPath
                if (treasureIntersections.size() == 2) {
                    if (treasureIntersections.get(0).distance(searchPath.getPoints().get(i)) <
                            treasureIntersections.get(1).distance(searchPath.getPoints().get(i))) {
                        cutSearchPath.addPoint(treasureIntersections.get(0));
                    } else {
                        cutSearchPath.addPoint(treasureIntersections.get(1));
                    }
                } else if (treasureIntersections.size() == 1) {
                    cutSearchPath.addPoint(treasureIntersections.get(0));
                } else {
                    /**
                     * got some rounding issue here.
                     * Our line located the treasure, but we could not found the intersection between the line and the {@link GameEngine#SCANNING_DISTANCE}.
                     * Thus, we choose the point on the line, which is the closest to the treasure.
                     */
                    LineSegment lineSegment = new LineSegment(searchPath.getPoints().get(i).getCoordinate(), searchPath.getPoints().get(i + 1).getCoordinate());
                    cutSearchPath.addPoint(JTSUtils.createPoint(lineSegment.closestPoint(treasurePos.getCoordinate())));
                }
                return cutSearchPath;
            }
        }
        throw new IllegalStateException("The Searcher located the treasure, but a second test failed.\n" +
                "This must be an rounding error in searchPath.getLines().get(i).distance(treasurePos)");
    }

    /**
     * Initializes {@link Searcher}, {@link Hider} and the treasure position
     * and simulates an initial Step.
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
            return new Turn(null, cutSearchPath(lastSearchPath, treasurePos), treasurePos);
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
     * @param previousAngleHint the previous {@link AngleHint}
     * @param currentAngleHint  {@link AngleHint} to be verified
     * @param treasurePosition  {@link Point} of the treasure position
     * @param searcherPosition  {@link Point} of the searcher position
     * @throws IllegalArgumentException if the {@link AngleHint} {@code angleHint} did not followed the rules.
     */
    protected static void verifyHint(AngleHint previousAngleHint, AngleHint currentAngleHint, Point treasurePosition, Point searcherPosition) {
        if (currentAngleHint == null) {
            throw new IllegalArgumentException("Hider gave a Hint, which was null!");
        }
        if (!currentAngleHint.getGeometryAngle().getCenter().equals(searcherPosition.getCoordinate())) {
            throw new IllegalArgumentException("AngleHint center do not lie on the player position.");
        }
        GeometryAngle geometryAngle = (currentAngleHint).getGeometryAngle();
        if (!geometryAngle.inView(treasurePosition.getCoordinate())) {
            throw new IllegalArgumentException("Treasure does not lie in given Angle.");
        }
        if (!JTSUtils.doubleEqual(geometryAngle.getCenter().distance(searcherPosition.getCoordinate()), 0)) {
            throw new IllegalArgumentException("Treasure does not originate in the searcher's last position.");
        }
    }

    /**
     * Verifies whether the {@link CircleHint} {@code circleHint} given by the {@link Hider} followed the given rules.
     *
     * @param previousCircleHint the previous {@link CircleHint}
     * @param currentCircleHint  {@link CircleHint} to be verified
     * @param treasurePosition   {@link Point} of the treasure position
     * @param searcherPosition   {@link Point} of the {@link Searcher} position
     * @throws IllegalArgumentException if the {@link CircleHint} {@code circleHint} did not followed the rules
     */
    protected static void verifyHint(CircleHint previousCircleHint, CircleHint currentCircleHint, Point treasurePosition, Point searcherPosition) {
        if (currentCircleHint == null) {
            throw new IllegalArgumentException("Hider gave a CircleHint, which was null!");
        }
        Circle currentCircle = currentCircleHint.getCircle();
        if (!currentCircle.inside(treasurePosition.getCoordinate())) {
            throw new IllegalArgumentException("The CircleHint does not contain the treasure.\n" +
                    "It says, " + currentCircle.getRadius() + " around " + currentCircle.getCenter() + ", " +
                    "but was " + currentCircle.getCenter().distance(treasurePosition.getCoordinate()));
        }
        if (previousCircleHint != null && !previousCircleHint.getCircle().contains(currentCircle)) {
            throw new IllegalArgumentException("The previous CircleHint does not contain the current CircleHint!");
        }
    }

    /**
     * Verifies whether the {@link Hint} {@code hint} given by the {@link Hider} followed the given rules.
     *
     * @param previousHint     the previous {@link Hint}
     * @param currentHint      {@link Hint} to be verified
     * @param treasurePosition {@link Point} of the treasure position
     * @param searcherPosition {@link Point} of the searcher position
     * @throws IllegalArgumentException if the {@link Hint} {@code hint} did not followed the rules.
     */
    protected static void verifyHint(Hint previousHint, Hint currentHint, Point treasurePosition, Point searcherPosition) {
        if (previousHint != null && !previousHint.getClass().equals(currentHint.getClass())) {
            throw new IllegalArgumentException("previous and current Hint are of different types!");
        }
        if (currentHint instanceof AngleHint) {
            verifyHint((AngleHint) previousHint, (AngleHint) currentHint, treasurePosition, searcherPosition);
        } else if (currentHint instanceof CircleHint) {
            verifyHint((CircleHint) previousHint, (CircleHint) currentHint, treasurePosition, searcherPosition);
        } else {
            throw new IllegalArgumentException("This type of hint is not known!");
        }
    }

    /**
     * Let the {@link GameEngine#hider} give its {@link Hint}.
     */
    protected void hiderMove() {
        Hint newHint = hider.move(lastSearchPath);
        verifyHint(lastHint, newHint, treasurePos, lastSearchPath.getLastPoint());
        lastHint = newHint;
    }
}
