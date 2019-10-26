package com.treasure.hunt.game;

import com.treasure.hunt.strategy.seeker.Moves;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.tipster.Tipster;
import com.treasure.hunt.strategy.seeker.Seeker;
import com.treasure.hunt.view.in_game.View;
import lombok.AllArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class GameManager {

    // final variavles
    private final GeometryFactory gf = new GeometryFactory();
    private final GameHistory gameHistory = new GameHistory();
    private final Seeker seeker;
    private final Tipster tipster;
    private final List<Thread> threads;

    // Game variables
    boolean finished = false;
    private Point seekerPos;
    private Point treasurePos;
    private Hint lastHint;

    public GameManager(Seeker seeker, Tipster tipster, List<View> view) {
        this.seeker = seeker;
        this.tipster = tipster;
        this.threads = new ArrayList<>();
        for (int i = 0; i < view.size(); i++) {
            Thread thread = new Thread((Runnable) view, "" + i);
            thread.start();
            threads.add(thread);
        }

        seekerPos = gf.createPoint(new Coordinate(0, 0));
        seeker.init(seekerPos, gameHistory);
        tipster.init(gameHistory);

        Moves moves = seeker.move();
        Hint lastHint = tipster.move(moves);
    }

    /**
     * This simulates just one step of the simulation.
     */
    public void step() {
        Moves moves = seeker.move(lastHint);
        lastHint = tipster.move(moves);
    }

    /**
     * This simulates the whole game, until its finished.
     */
    public void run() {
        while (!finished)
            step();
    }

    /**
     * Simulates a fixed number of steps.
     *
     * @param steps number of steps
     */
    public void run(int steps) {
        for (int i = 0; i < steps; i++) step();
    }

    // TODO handle exception
    public void quit() throws InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
