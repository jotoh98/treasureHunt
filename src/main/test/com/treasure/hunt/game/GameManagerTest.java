package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hider.impl.InstantWinHider;
import com.treasure.hunt.strategy.hider.impl.RevealingHider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.impl.MoveOverTreasure1Searcher;
import com.treasure.hunt.strategy.searcher.impl.MoveOverTreasure2Searcher;
import com.treasure.hunt.strategy.searcher.impl.NaiveSearcher;
import com.treasure.hunt.strategy.searcher.impl.StandingSearcher;
import com.treasure.hunt.view.in_game.View;
import com.treasure.hunt.view.in_game.impl.ConsoleOutputView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GameManagerTest {
    private List<View> views = new ArrayList<>();

    @BeforeEach
    void setUp() {
        views.add(new ConsoleOutputView());
    }

    /**
     * Game simulation test:
     * {@link RevealingHider} gives perfect hint.
     * {@link NaiveSearcher} follows.
     * {@link GameManager#isFinished()} should now return true.
     */
    @Test
    void spoiledGame() {
        GameManager gameManager = new GameManager(new NaiveSearcher(), new RevealingHider(), views);
        gameManager.init();
        gameManager.run(2);
        assertTrue(gameManager.isFinished());
    }

    @Test
    void moveOnTreasure() {
        GameManager gameManager = new GameManager(new NaiveSearcher(), new RevealingHider(), views);
        gameManager.init();
        gameManager.run(2);
        assertTrue(gameManager.isFinished());
    }

    @Test
    void moveOverTreasure1() {
        GameManager gameManager = new GameManager(new MoveOverTreasure1Searcher(), new RevealingHider(), views);
        gameManager.init();
        gameManager.run(2);
        assertTrue(gameManager.isFinished());
    }

    @Test
    void moveOverTreasure2() {
        GameManager gameManager = new GameManager(new MoveOverTreasure2Searcher(), new RevealingHider(), views);
        gameManager.init();
        gameManager.run(2);
        assertTrue(gameManager.isFinished());
    }

    @Test
    void spawnOnTreasure() {
        GameManager gameManager = new GameManager(new StandingSearcher(), new InstantWinHider(), views);
        gameManager.init();
        gameManager.run(0);
        assertTrue(gameManager.isFinished());
    }

    /**
     * {@link GameManager#located(List)} )} test.
     * In this test, the searcher moves <b>past</b> the treasure
     * with a minimum distance of 1.
     * searcher starts at (0,0) as usual.
     * treasure spawns at (1,1).
     * hider tells the point (2,0).
     */
    @Test
    void narrowMove() {
        GameManager gameManager = new GameManager(new NaiveSearcher(), new Hider() {

            private GeometryFactory gf = new GeometryFactory();
            private Point treasurePos = gf.createPoint(new Coordinate(1, 1));

            @Override
            public Point getTreasureLocation() {
                return treasurePos;
            }

            @Override
            public Hint move(Movement moves) {
                CircleHint hint = new CircleHint(gf.createPoint(new Coordinate(0, 2)), 0);
                return hint;
            }
        }, views);
        gameManager.init();
        gameManager.run(2);
        assertTrue(gameManager.isFinished());
    }
}