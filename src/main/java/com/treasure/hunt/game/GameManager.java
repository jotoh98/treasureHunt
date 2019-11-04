package com.treasure.hunt.game;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;
import com.treasure.hunt.view.in_game.View;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.util.List;

/**
 * This is the GameManager which should be started,
 * to start a normal game.
 */
@Requires(hider = Hider.class, searcher = Searcher.class)
public class GameManager {

    /**
     * Final variables
     */
    protected final GeometryFactory gf = new GeometryFactory();
    protected final GameHistory gameHistory = new GameHistory();
    protected final Searcher searcher;
    protected final Hider hider;

    /**
     * Safe, whether the game is done or not.
     */
    protected boolean finished = false;

    /**
     * Game variables
     */
    protected Hint lastHint;
    protected Moves lastMoves;

    /**
     * View variables
     */
    private List<View> view;
    protected Point searcherPos;
    protected Point treasurePos;
    /**
     * This tells, whether the next step is the first or not.
     */
    protected boolean firstStep;

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
            lastMoves = searcher.move();
            firstStep = false;
        } else {
            lastMoves = searcher.move(lastHint);
        }
        searcherPos = lastMoves.getEndPoint().getObject();
        if (located()) {
            finished = true;
            return;
        }
        lastHint = hider.move(lastMoves);
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
     * @return whether the performed {@link Moves}' by the searcher and {@link Hint}'s from the hider were correct.
     */
    protected boolean checkConsistency() {
        // TODO implement
        return true;
    }

    /**
     * @return whether the searcher located the treasure successfully.
     */
    protected boolean located() {
        Point lastPoint = null;
        for (GeometryItem<Point> geometriItem : lastMoves.getPoints()) {
            Point point = geometriItem.getObject();
            if (lastPoint == null) {
                lastPoint = point;
            } else {
                // Check the gap of each move-segment and treasurePos
                LineSegment lineSegment = new LineSegment(new Coordinate(lastPoint.getX(), lastPoint.getY()),
                        new Coordinate(point.getX(), point.getY()));
                if (lineSegment.distancePerpendicular(new Coordinate(treasurePos.getX(), treasurePos.getY())) <= 1) {
                    // searcher found the treasure
                    finished = true;
                }
            }
        }
        return finished;
    }

    /**
     * This initializes positions, searcher and hider.
     */
    protected void init() {
        for (View view : view) { // TODO does this go better ?
            gameHistory.registerListener(view);
        }
        searcherPos = gf.createPoint(new Coordinate(0, 0));
        treasurePos = gf.createPoint(new Coordinate(0, 0));
        searcher.init(searcherPos, gameHistory);
        hider.init(treasurePos, gameHistory);
    }
}
