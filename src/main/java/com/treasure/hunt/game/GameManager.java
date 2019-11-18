package com.treasure.hunt.game;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.Requires;
import com.treasure.hunt.view.in_game.View;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.util.List;

/**
 * This is the GameManager which should be started,
 * to start a normal game.
 */
@Requires(hider = Hider.class, searcher = Searcher.class)
public class GameManager {

    protected List<View> view;
    protected final GameHistory gameHistory = new GameHistory();

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
    /**
     * This tells, whether the next step is the first or not.
     */
    protected boolean firstStep = true;

    public GameManager(Searcher searcher, Hider hider, List<View> view) {
        this.searcher = searcher;
        this.hider = hider;
        this.view = view;
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
    public void step() {
        if (finished) {
            throw new IllegalStateException("Game is already finished");
        }
        if (firstStep) {
            lastMovement = searcher.move();
            firstStep = false;
        } else {
            lastMovement = searcher.move(lastHint);
        }
        searcherPos = lastMovement.getEndPoint().getObject();
        if (located()) {
            finished = true;
        } else {
            lastHint = hider.move(lastMovement);
        }
        if (!checkConsistency()) {
            throw new IllegalStateException("Game is no longer consistent!");
        }
        gameHistory.dump(new Move(lastHint, lastMovement, treasurePos));
    }

    /**
     * This simulates the whole game, until its finished.
     */
    public void run() {
        while (!finished) {
            step();
        }
    }

    /**
     * Simulates a fixed number of steps.
     * Breaks, when the game is finished.
     *
     * @param steps number of steps
     */
    public void run(int steps) {
        for (int i = 0; i < steps; i++) {
            if (finished) {
                break;
            }
            step();
        }
    }

    /**
     * @return whether the performed {@link Movement}' by the searcher and {@link Hint}'s from the hider were correct.
     */
    protected boolean checkConsistency() {
        // TODO implement
        // forbid wrong hints
        return true;
    }

    /**
     * @return whether the searcher located the treasure successfully.
     */
    protected boolean located() {
        Point lastPoint = null;
        for (GeometryItem<Point> geometryItem : lastMovement.getPoints()) {
            Point point = geometryItem.getObject();
            if (lastPoint == null) {
                lastPoint = point;
            } else {
                // Check the gap of each move-segment and treasurePos
                LineSegment lineSegment = new LineSegment(new Coordinate(lastPoint.getX(), lastPoint.getY()),
                        new Coordinate(point.getX(), point.getY()));
                // Usage of distancePerpendicular is completely incorrect here, since the line will be infinite
                if (lineSegment.distance(new Coordinate(treasurePos.getX(), treasurePos.getY())) <= 1) {
                    // distance to treasure is <= 1
                    // searcher found the treasure
                    finished = true;
                }
            }
        }
        assert (finished ||
                ((searcherPos.getX() != treasurePos.getX()) && (searcherPos.getY() != treasurePos.getY())));
        return finished;
    }

    /**
     * register View-Threads.
     * initialize searcher and hider.
     * initialize searcher and treasure positions.
     */
    public void init() {
        for (View view : view) {
            gameHistory.registerListener(view);
            view.init(gameHistory);
        }

        searcherPos = JTSUtils.getDefaultGeometryFactory().createPoint(new Coordinate(0, 0));
        searcher.init(searcherPos);

        treasurePos = hider.getTreasureLocation();
        gameHistory.dump(
                new Move(null, null, treasurePos)
        );
    }

    protected void outitialize() {
        gameHistory.waitForListeners();
    }
}
