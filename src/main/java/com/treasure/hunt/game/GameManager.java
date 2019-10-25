package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.tipster.Tipster;
import com.treasure.hunt.strategy.seeker.Moves;
import com.treasure.hunt.strategy.seeker.Seeker;
import com.treasure.hunt.ui.in_game.View;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

@RequiredArgsConstructor
public class GameManager {
    // Players
    private final Tipster tipster;
    private final Seeker seeker;

    // Render variables
    private final View view;
    private final GameHistory gameHistory;

    // Geometries
    private GeometryFactory gf = new GeometryFactory();
    private Point seekerPos;
    private Point treasurePos;

    // Game variables
    private Hint lastHint;

    public GameManager(Seeker seeker, Tipster tipster, View view) {
        this.tipster = tipster;
        this.seeker = seeker;
        this.view = view; // TODO add custom uiRenderer
        gameHistory = new GameHistory();
    }

    /**
     * This simulates the next step where
     * the Player does some moves until it
     * asks for a hint.
     * Then the HintGiver (switches the
     * treasure position) and gives a hint.
     */
    public void onNext() {
        Moves moves = seeker.generate(lastHint);
        gameHistory.dump(seeker.getAvailableVisualisationGeometryItems());
        tipster.generateHint(moves);
    }

    /**
     * This starts the Simulation
     * until the end.
     */
    public void start() {
        // Init seeker and tipster
        this.seekerPos = gf.createPoint(new Coordinate(0, 0));
        seeker.init(this.seekerPos);
        tipster.init(1);
        this.treasurePos = tipster.getTreasureLocation();

        Moves moves = seeker.generate();
        gameHistory.dump(seeker.getAvailableVisualisationGeometryItems());
        lastHint = tipster.generateHint(moves);

        // start the game loop
        boolean done = false;
        while (!done) {
            onNext();
            done = true;
        }
    }
}
