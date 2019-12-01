package com.treasure.hunt.game;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.Requires;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the engine which runs a simulation of a treasure hunt.
 */
@Requires(hider = Hider.class, searcher = Searcher.class)
public class GameEngine {

    protected final Searcher searcher;
    protected final Hider hider;
    /**
     * Safe, whether the game is done or not.
     */
    @Getter
    protected boolean finished = false;
    protected Hint lastHint;
    protected Movement lastMovement;
    protected Point searcherPos;
    protected Point treasurePos;

    protected boolean firstMove = true;

    public GameEngine(Searcher searcher, Hider hider) {
        this.searcher = searcher;
        this.hider = hider;
    }

    /**
     * This simulates just one step of the simulation.
     * The searcher begins since we want not force him,
     * to take a initial hint, he eventually do not need,
     * if he works randomized!
     * <p>
     * Updates the searchers position.
     * <p>
     * The first step of the searcher goes without an hint,
     * the next will be with.
     */
    public Move move() {
        if (finished) {
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
        searcherPos = lastMovement.getEndPoint();

        if (located(lastMovement.getPoints())) {
            finish();
            return new Move(null, lastMovement, treasurePos);
        } else {
            lastHint = hider.move(lastMovement);
        }
        assert (lastHint != null);

        if (!checkConsistency()) {
            throw new IllegalStateException("Game is no longer consistent!");
        }
        System.out.println("" +
                "treasurePos: " + treasurePos +
                "searcherPos:  " + searcherPos +
                "");
        return new Move(lastHint, lastMovement, treasurePos);
    }

    /**
     * This simulates the whole game, until its finished.
     */
    public void beat() {
        while (!finished) {
            move();
        }
    }

    /**
     * Simulates a fixed number of steps.
     * Breaks, when the game is finished.
     *
     * @param steps number of steps
     */
    public void move(int steps) {
        for (int i = 0; i < steps; i++) {
            if (finished) {
                break;
            }
            move();
        }
    }

    /**
     * @return whether the performed {@link Movement}' by the searcher and {@link Hint}'s from the hider were correct.
     */
    protected boolean checkConsistency() {
        // TODO implement
        // AngleHints must be correct
        // AngleHints <=180 degrees
        // CircleHints correct
        // CircleHints must contain eachother !?
        return true;
    }

    /**
     * @return whether the searcher located the treasure successfully.
     */
    protected boolean located(List<GeometryItem<Point>> geometryItemsList) {
        assert geometryItemsList.size() > 0;

        // Did the searcher move ?
        if (geometryItemsList.size() == 1) {
            return geometryItemsList.get(0).getObject().distance(treasurePos) <= 1;
        } else {
            Point lastPoint = null;
            for (GeometryItem<Point> geometryItem : geometryItemsList) {
                Point point = geometryItem.getObject();
                if (lastPoint == null) {
                    lastPoint = point;
                } else {
                    // Check the gap of each move-segment and treasurePos
                    LineSegment lineSegment = new LineSegment(new Coordinate(lastPoint.getX(), lastPoint.getY()),
                            new Coordinate(point.getX(), point.getY()));
                    // Usage of distancePerpendicular is completely incorrect here, since the line will be infinite
                    if (lineSegment.distance(new Coordinate(treasurePos.getX(), treasurePos.getY())) <= 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * initialize searcher and hider.
     * initialize searcher and treasure positions.
     */
    public Move init() {
        searcherPos = JTSUtils.createPoint(0, 0);
        searcher.init(searcherPos);

        treasurePos = hider.getTreasureLocation();
        assert (treasurePos != null);

        // Check, whether treasure spawns in range of searcher
        List<GeometryItem<Point>> act = new ArrayList<>();
        act.add(new GeometryItem<>(searcherPos, GeometryType.WAY_POINT));
        if (located(act)) {
            finish();
        }

        return new Move(
                null,
                new Movement(searcherPos),
                treasurePos);
    }

    protected void finish() {
        finished = true;
    }
}
