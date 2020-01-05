package com.treasure.hunt.game;

import com.treasure.hunt.analysis.Statistic;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.Requires;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.util.Collections;
import java.util.List;

/**
 * This is the engine which runs a simulation of a treasure hunt.
 *
 * @author dorianreineccius
 */
@Requires(hider = Hider.class, searcher = Searcher.class)
public class GameEngine {
    /**
     * The height of the playing area.
     */
    @Getter
    protected final int height;
    /**
     * The width of the playing area.
     */
    @Getter
    protected final int width;
    protected final Coordinate initialSearcherCoordinate;
    protected final Searcher searcher;
    protected final Hider hider;
    @Getter
    private final Statistic statistics = new Statistic();
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
    protected Movement lastMovement;
    protected Point searcherPos;
    protected Point treasurePos;

    /**
     * The constructor.
     *
     * @param searcher playing the game
     * @param hider    playing the game
     */
    public GameEngine(Searcher searcher, Hider hider) {
        this(searcher, hider, 200, 200);
    }

    /**
     * The constructor.
     *
     * @param searcher playing the game
     * @param hider    playing the game
     * @param width    the width of the playing area
     * @param height   the height of the playing area
     */
    public GameEngine(Searcher searcher, Hider hider, int width, int height) {
        this(searcher, hider, new Coordinate(0, 0), width, height);
    }

    /**
     * The constructor.
     *
     * @param searcher                  playing the game
     * @param hider                     playing the game
     * @param initialSearcherCoordinate the initial Searcher {@link Coordinate}.
     */
    public GameEngine(Searcher searcher, Hider hider, Coordinate initialSearcherCoordinate) {
        this(searcher, hider, initialSearcherCoordinate, 200, 200);
    }

    /**
     * The constructor.
     *
     * @param searcher                  playing the game
     * @param hider                     playing the game
     * @param initialSearcherCoordinate the initial Searcher {@link Coordinate}.
     * @param width                     the width of the playing area
     * @param height                    the height of the playing area
     */
    public GameEngine(Searcher searcher, Hider hider, Coordinate initialSearcherCoordinate, int width, int height) {
        this.searcher = searcher;
        this.hider = hider;
        this.initialSearcherCoordinate = initialSearcherCoordinate;
        this.width = width;
        this.height = height;
        if (outOfMap(initialSearcherCoordinate)) {
            throw new IllegalArgumentException("initialSearcherCoordinate: " + initialSearcherCoordinate + " lies outside the playing area.");
        }
    }

    /**
     * @param geometryItemsList searcher path
     * @param treasurePosition  the position of the treasure
     * @return {@code true} if the searcher located the treasure successfully. {@code false}, otherwise.
     */
    protected static boolean located(List<GeometryItem<Point>> geometryItemsList, Point treasurePosition) {
        assert geometryItemsList.size() > 0;

        // Did the searcher move ?
        if (geometryItemsList.size() == 1) {
            return geometryItemsList.get(0).getGeometry().distance(treasurePosition) <= 1;
        } else {
            Point lastPoint = null;
            Point point;
            for (GeometryItem<Point> geometryItem : geometryItemsList) {
                point = geometryItem.getGeometry();
                if (lastPoint == null) {
                    lastPoint = point;
                } else {
                    // Check the gap of each move-segment and treasurePos
                    LineSegment lineSegment = new LineSegment(new Coordinate(lastPoint.getX(), lastPoint.getY()),
                            new Coordinate(point.getX(), point.getY()));
                    // Usage of distancePerpendicular is completely incorrect here, since the line will be infinite
                    if (lineSegment.distance(new Coordinate(treasurePosition.getX(), treasurePosition.getY())) <= 1) {
                        return true;
                    }
                    lastPoint = point;
                }
            }
        }
        return false;
    }

    /**
     * initialize searcher and treasure positions.
     *
     * @return a {@link Move}, since the initialization must be displayed.
     */
    public Move init() {
        searcherPos = JTSUtils.GEOMETRY_FACTORY.createPoint(initialSearcherCoordinate);
        searcher.init(searcherPos, width, height);
        hider.init(searcherPos, width, height);

        treasurePos = hider.getTreasureLocation();
        if (treasurePos == null) {
            throw new IllegalArgumentException(hider + " gave a treasurePosition which is null.");
        }
        if (outOfMap(treasurePos.getCoordinate())) {
            throw new IllegalArgumentException("hider" + hider + " gave a treasure position which lies outside the playing area.");
        }

        // Check, whether treasure spawns in range of searcher
        if (located(Collections.singletonList(new GeometryItem<>(searcherPos, GeometryType.WAY_POINT)), treasurePos)) {
            setFinished(true);
        }

        return new Move(
                null,
                new Movement(searcherPos),
                treasurePos);
    }

    /**
     * This simulates just one step of the simulation.
     * The searcher begins since we want not force him,
     * to take a initial hint, he eventually do not need,
     * f.e. if he works randomized!
     *
     * @return the {@link Move}, happened in this step.
     */
    public Move move() {
        if (finished) {
            throw new IllegalStateException("Game is already finished");
        }

        searcherMove();

        if (located(lastMovement.getPoints(), treasurePos)) {
            setFinished(true);
            return new Move(null, lastMovement, treasurePos);
        } else {
            hiderMove();
        }

        return new Move(lastHint, lastMovement, treasurePos);
    }

    /**
     * Let the {@link GameEngine#hider} give its {@link Hint}.
     */
    protected void hiderMove() {
        lastHint = hider.move(lastMovement);
        assert (lastHint != null);
        verifyHint(lastHint, treasurePos);
    }

    /**
     * Let the {@link GameEngine#searcher} make {@link Movement}.
     */
    protected void searcherMove() {
        if (firstMove) {
            firstMove = false;
            lastMovement = searcher.move();
        } else {
            lastMovement = searcher.move(lastHint);
        }
        assert (lastMovement != null);
        assert (lastMovement.getPoints().size() != 0);
        verifyMovement(lastMovement, searcherPos);

        searcherPos = lastMovement.getEndPoint();
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
    protected void verifyHint(Hint hint, Point treasurePosition) {
        if (hint instanceof AngleHint) {
            if (!((AngleHint) hint).getGeometryAngle().inView(treasurePosition.getCoordinate())) {
                throw new IllegalArgumentException("Treasure does not lie in given Angle.");
            }
        }
        if (hint instanceof CircleHint) {
            if (((CircleHint) hint).getRadius() < ((CircleHint) hint).getCenter().distance(treasurePosition)) {
                throw new IllegalArgumentException("The CircleHint does not contain the treasure.\n" +
                        "It says, " + ((CircleHint) hint).getRadius() + " around " + ((CircleHint) hint).getCenter() + ", " +
                        "but was " + ((CircleHint) hint).getCenter().distance(treasurePosition));
            }
        }
    }

    /**
     * Verifies whether the performed {@link Movement} {@code movement} by the {@link Searcher} followed the rules.
     *
     * @param movement                which gets verified
     * @param initialSearcherPosition initial searcher position
     * @throws IllegalArgumentException when the {@link Movement} is not valid.
     */
    protected void verifyMovement(Movement movement, Point initialSearcherPosition) {
        if (movement.getStartingPoint().getX() != initialSearcherPosition.getX() ||
                movement.getStartingPoint().getY() != initialSearcherPosition.getY()) {
            throw new IllegalArgumentException("Searcher stands last at " + initialSearcherPosition +
                    " but continues his movement from " + movement.getStartingPoint());
        }
        for (GeometryItem geometryItem : movement.getPoints()) {
            if (outOfMap(geometryItem.getGeometry().getCoordinate())) {
                throw new IllegalArgumentException("Searcher left the playing area: " +
                        "(" + ((Point) geometryItem.getGeometry()).getX() + ", " + ((Point) geometryItem.getGeometry()).getY() + ") " +
                        "is not in " + "[" + -width / 2 + ", " + width / 2 + "]x[" + -height / 2 + ", " + height / 2 + "]");
            }
        }
    }

    /**
     * @param coordinate the {@link Coordinate}, we want to test, whether it lies outside the playing area.
     * @return {@code true}, if the {@code coordinate} lies outside the playing area. {@code false}, otherwise.
     */
    public boolean outOfMap(Coordinate coordinate) {
        return coordinate.x < (float) -width / 2 ||
                (float) width / 2 < coordinate.x ||
                coordinate.y < (float) -height / 2 ||
                (float) height / 2 < coordinate.y;
    }
}
