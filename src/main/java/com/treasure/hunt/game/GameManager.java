package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.seeker.Moves;
import com.treasure.hunt.strategy.seeker.Seeker;
import com.treasure.hunt.strategy.tipster.Tipster;
import com.treasure.hunt.view.in_game.View;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.List;

/**
 * This is the GameManager which should be started,
 * to start a normal game.
 */
public class GameManager {

    // final variables
    protected final GeometryFactory gf = new GeometryFactory();
    protected final GameHistory gameHistory = new GameHistory();
    protected final Seeker seeker;
    protected final Tipster tipster;

    // Game variables
    protected boolean finished = false;
    protected Point seekerPos;
    protected Point treasurePos;
    protected Hint lastHint;
    /**
     * This tells, whether the next step is the first or not.
     */
    protected boolean firstStep;

    public GameManager(Seeker seeker, Tipster tipster, List<View> view) {
        this.seeker = seeker;
        this.tipster = tipster;
    }

    /**
     * This simulates just one step of the simulation.
     * The seeker begins since we want not force him,
     * to take a initial hint, he eventually do not need,
     * if he works randomized!
     * <p>
     * The first step of the seeker goes without an hint,
     * the next will be with.
     */
    public void step() {
        Moves moves;
        if (firstStep)
            moves = seeker.move();
        else
            moves = seeker.move(lastHint);
        if (located()) {
            finished = true;
            return;
        }
        lastHint = tipster.move(moves);
    }

    /**
     * This simulates the whole game, until its finished.
     */
    public void run() {
        while (!finished) step();
    }

    /**
     * Simulates a fixed number of steps.
     *
     * @param steps number of steps
     */
    public void run(int steps) {
        for (int i = 0; i < steps; i++) {
            if (finished) break;
            step();
        }
    }

    /**
     * @return whether the performed moves by the seeker/hints from the tipster were correct.
     */
    protected boolean checkConsistency() {
        // TODO implement
        return true;
    }

    /**
     * @return whether the seeker located the treasure successfully.
     */
    protected boolean located() {
        // TODO implement
        return false;
    }

    /**
     * Use this to initialize seeker, tipster and
     * start the concurrent threads.
     */
    protected void init() {
        seekerPos = gf.createPoint(new Coordinate(0, 0));
        treasurePos = gf.createPoint(new Coordinate(0, 0));
        seeker.init(seekerPos, gameHistory);
        tipster.init(treasurePos, gameHistory);
    }
}
