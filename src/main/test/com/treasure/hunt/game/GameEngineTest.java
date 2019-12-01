package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hider.impl.InstantWinHider;
import com.treasure.hunt.strategy.hider.impl.RevealingHider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.impl.MoveOverTreasure1Searcher;
import com.treasure.hunt.strategy.searcher.impl.MoveOverTreasure2Searcher;
import com.treasure.hunt.strategy.searcher.impl.NaiveCircleSearcher;
import com.treasure.hunt.strategy.searcher.impl.StandingSearcher;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineTest {

    /**
     * Game simulation test:
     * {@link RevealingHider} gives perfect hint.
     * {@link NaiveCircleSearcher} follows.
     * {@link GameEngine#isFinished()} should now return true.
     */
    @Test
    void spoiledGame() {
        GameEngine gameEngine = new GameEngine(new NaiveCircleSearcher(), new RevealingHider());
        gameEngine.init();
        gameEngine.move(2);
        assertTrue(gameEngine.isFinished());
    }

    @Test
    void moveOnTreasure() {
        GameEngine gameEngine = new GameEngine(new NaiveCircleSearcher(), new RevealingHider());
        gameEngine.init();
        gameEngine.move(2);
        assertTrue(gameEngine.isFinished());
    }

    @Test
    void moveOverTreasure1() {
        GameEngine gameEngine = new GameEngine(new MoveOverTreasure1Searcher(), new RevealingHider());
        gameEngine.init();
        gameEngine.move(2);
        assertTrue(gameEngine.isFinished());
    }

    @Test
    void moveOverTreasure2() {
        GameEngine gameEngine = new GameEngine(new MoveOverTreasure2Searcher(), new RevealingHider());
        gameEngine.init();
        gameEngine.move(2);
        assertTrue(gameEngine.isFinished());
    }

    @Test
    void spawnOnTreasure() {
        GameEngine gameEngine = new GameEngine(new StandingSearcher(), new InstantWinHider());
        gameEngine.init();
        gameEngine.move(0);
        assertTrue(gameEngine.isFinished());
    }

    /**
     * {@link GameEngine#located(List)} )} test.
     * In this test, the searcher moves <b>past</b> the treasure
     * with a minimum distance of 1.
     * searcher starts at (0,0) as usual.
     * treasure spawns at (1,1).
     * hider tells the point (2,0).
     */
    @Test
    void narrowMove() {
        GameEngine gameEngine = new GameEngine(new NaiveCircleSearcher(), new Hider() {

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
        });
        gameEngine.init();
        gameEngine.move(2);
        assertTrue(gameEngine.isFinished());
    }
}