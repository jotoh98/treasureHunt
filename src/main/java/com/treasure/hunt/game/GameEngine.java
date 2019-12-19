package com.treasure.hunt.game;

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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Getter
@Setter
public class GameEngine {
    public static final int HEIGHT = 200;
    public static final int WIDTH = 200;

    protected final Searcher searcher;
    protected final Hider hider;
    /**
     * Tells, whether the game is done or not.
     */
    @Getter
    private BooleanProperty finished = new SimpleBooleanProperty(false);
    protected Hint lastHint;
    protected Movement lastMovement;
    protected Point searcherPos;
    protected Point treasurePos;
    /**
     * Tells, whether a first move is happened in the game yet, or not.
     */
    protected boolean firstMove = true;

    /**
     * @param searcher playing the game
     * @param hider    playing the game
     */
    public GameEngine(Searcher searcher, Hider hider) {
        this.searcher = searcher;
        this.hider = hider;
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
            return geometryItemsList.get(0).getObject().distance(treasurePosition) <= 1;
        } else {
            Point lastPoint = null;
            Point point;
            for (GeometryItem<Point> geometryItem : geometryItemsList) {
                point = geometryItem.getObject();
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

    public Move init() {
        return init(JTSUtils.createPoint(0, 0));
    }

    /**
     * initialize searcher and hider.
     * initialize searcher and treasure positions.
     *
     * @param p initial searcher position
     * @return a {@link Move}, since the initialization must be displayed.
     */
    public Move init(Point p) {
        searcherPos = p;
        searcher.init(searcherPos);

        treasurePos = hider.getTreasurePos();
        if (treasurePos == null) {
            throw new IllegalArgumentException(hider + " gave a treasurePosition which is null.");
        }

        // Check, whether treasure spawns in range of searcher
        if (located(Collections.singletonList(new GeometryItem<>(searcherPos, GeometryType.WAY_POINT)), treasurePos)) {
            finish();
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
     * <p>
     * Updates the searchers position.
     *
     * @return the {@link Move}, happened in this step.
     */
    public Move move() {
        searcherMove();

        if (located(lastMovement.getPoints(), treasurePos)) {
            finish();
            return new Move(null, lastMovement, treasurePos);
        } else {
            hiderMove();
        }

        return new Move(lastHint, lastMovement, treasurePos);
    }

    /**
     * Lets the {@link GameEngine#hider} make its move.
     */
    protected void hiderMove() {
        lastHint = hider.move(lastMovement);
        assert (lastHint != null);
        verifyHint(lastHint, treasurePos);
    }

    /**
     * Lets the {@link GameEngine#searcher} make its move.
     */
    protected void searcherMove() {
        if (finished.get()) {
            throw new IllegalStateException("Game is already finished");
        }

        // Searcher moves
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
            if (((Point) geometryItem.getObject()).getX() < (float) -WIDTH / 2 ||
                    (float) WIDTH / 2 < ((Point) geometryItem.getObject()).getX() ||
                    ((Point) geometryItem.getObject()).getY() < (float) -HEIGHT / 2 ||
                    (float) HEIGHT / 2 < ((Point) geometryItem.getObject()).getY()) {
                throw new IllegalArgumentException("Searcher left the playing area: " +
                        "(" + ((Point) geometryItem.getObject()).getX() + ", " + ((Point) geometryItem.getObject()).getY() + ") " +
                        "is not in " + "[" + -WIDTH / 2 + ", " + WIDTH / 2 + "]x[" + -HEIGHT / 2 + ", " + HEIGHT / 2 + "]");
            }
        }
    }

    /**
     * TODO implement:
     * AngleHints must be correct
     * AngleHints must be of angle [0, 180] !?
     * CircleHints must contain each other !?
     * Verifies whether the performed {@link Movement}' by the searcher and {@link Hint}'s from the hider followed the rules.
     *
     * @param hint             hint to be verified
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
     * Sets {@link GameEngine#finished} to true.
     */
    protected void finish() {
        finished.set(true);
    }

    protected boolean isFinished() {
        return finished.get();
    }
}
