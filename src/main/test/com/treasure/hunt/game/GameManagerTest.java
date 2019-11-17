package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hider.implementations.SpoilerHider;
import com.treasure.hunt.strategy.hint.CircleHint;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.strategy.searcher.implementations.NaiveSearcher;
import com.treasure.hunt.view.in_game.View;
import com.treasure.hunt.view.in_game.implementatons.ConsoleOutputView;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GameManagerTest {

    private List<View> views = new ArrayList<>();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        views.add(new ConsoleOutputView());
    }

    /**
     * Game simulation test:
     * {@link SpoilerHider} gives perfect hint.
     * {@link NaiveSearcher} follows.
     * {@link GameManager#isFinished()} should now return true.
     */
    @Test
    void spoiledGame() {
        GameManager gameManager = new GameManager(new NaiveSearcher(), new SpoilerHider(), new ArrayList<>());
        gameManager.init();
        gameManager.run(2);
        assertTrue(gameManager.isFinished());
    }

    /**
     * {@link GameManager#located()} test.
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
            private GameHistory gameHistory;
            private Point treasurePos = gf.createPoint(new Coordinate(1, 1));

            @Override
            public Point getTreasureLocation() {
                return treasurePos;
            }

            @Override
            public void init(GameHistory gameHistory) {
                this.gameHistory = gameHistory;
            }

            @Override
            public Hint move(Moves moves) {
                CircleHint hint = new CircleHint(gf.createPoint(new Coordinate(0, 2)), 0);
                gameHistory.dump(hint);
                return hint;
            }
        }, views);
        gameManager.init();
        gameManager.run(2);
        assertTrue(gameManager.isFinished());
    }
}