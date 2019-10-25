package com.treasure.hunt.game;

import com.treasure.hunt.strategy.tipster.Tipster;
import com.treasure.hunt.strategy.seeker.Seeker;
import com.treasure.hunt.view.in_game.View;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class GameManager {
    private final GeometryFactory gf;
    private final Tipster tipster;
    private final Seeker seeker;
    private final View view;
    private final GameHistory gameHistory = new GameHistory();

    private Point seekerPos;
    private Point treasurePos;

    public GameManager(Tipster tipster, Seeker seeker, View view) {
        this.gf = new GeometryFactory();
        this.tipster = tipster;
        this.seeker = seeker;
        this.view = view;
    }

    public void next() {

    }

    public void run() {
        Thread thread = new Thread(view, "View");
        thread.start();

        tipster.init(gameHistory);
        seeker.init(gf.createPoint(new Coordinate(0, 0)), gameHistory);

        boolean done = false;
        while(!done) {
            next();
            done = true;
        }

    }
}
