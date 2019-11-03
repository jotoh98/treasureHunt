package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.Requires;
import com.treasure.hunt.view.in_game.View;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.List;

/**
 * This is the GameManager which should be started,
 * to start a normal game.
 */
@Requires(hider = Hider.class, searcher = Searcher.class)
public class GameManager {

    // final variables
    protected final GeometryFactory gf = new GeometryFactory();
    protected final GameHistory gameHistory = new GameHistory();
    protected final Searcher searcher;
    protected final Hider hider;

    // Game variables
    protected boolean finished = false;
    protected Point searcherPos;
    protected Point treasurePos;
    protected Hint lastHint;
    /**
     * This tells, whether the next step is the first or not.
     */
    protected boolean firstStep;

    public GameManager(Searcher searcher, Hider hider, List<View> view) {
        this.searcher = searcher;
        this.hider = hider;
    }

    /**
     * This simulates just one step of the simulation.
     * The searcher begins since we want not force him,
     * to take a initial hint, he eventually do not need,
     * if he works randomized!
     * <p>
     * The first step of the searcher goes without an hint,
     * the next will be with.
     */
    public void step() {
        Moves moves;
        if (firstStep) {
            moves = searcher.move();
        } else {
            moves = searcher.move(lastHint);
        }
        if (located()) {
            finished = true;
            return;
        }
        lastHint = hider.move(moves);
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
        // TODO implement
        return false;
    }

    /**
     * This initializes positions, searcher and hider.
     */
    protected void init() {
        searcherPos = gf.createPoint(new Coordinate(0, 0));
        treasurePos = gf.createPoint(new Coordinate(0, 0));
        searcher.init(searcherPos, gameHistory);
        hider.init(treasurePos, gameHistory);
    }
}
