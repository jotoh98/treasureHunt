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
public class Normal implements GameManager {

    // Geometry variables
    private final GeometryFactory gf = new GeometryFactory();

    // Players
    private final GameHistory gameHistory = new GameHistory();
    private Seeker seeker;
    private Tipster tipster;
    private List<Thread> threads;

    private Point seekerPos;
    private Point treasurePos;
    private Hint lastHint;

    public void init(Seeker seeker, Tipster tipster, List<View> view) {
        this.seeker = seeker;
        this.tipster = tipster;
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < view.size(); i++) {
            Thread thread = new Thread((Runnable) view, "" + i);
            thread.start();
            threads.add(thread);
        }
    }

    @Override
    public void next() {
        Moves moves = seeker.move(lastHint);
        lastHint = tipster.move(moves);
    }

    /**
     * This
     */
    @Override
    public void run() {
        seeker.init(gf.createPoint(new Coordinate(0, 0)), gameHistory);
        tipster.init(gameHistory);

        Moves moves = seeker.move();
        Hint lastHint = tipster.move(moves);
    }

    // TODO handle exception
    public void quit() throws InterruptedException {
        for(Thread thread : threads) {
            thread.join();
        }
    }
}
