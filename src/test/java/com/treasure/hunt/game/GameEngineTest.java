package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hider.impl.InstantWinHider;
import com.treasure.hunt.strategy.hider.impl.RevealingHider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.SearchPathPrototype;
import com.treasure.hunt.strategy.searcher.impl.*;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link GameEngine}.
 *
 * @author dorianreineccius
 */
@Slf4j
class GameEngineTest {

    /**
     * This simulates a fixed number of steps.
     * Breaks, when the game is finished.
     *
     * @param gameEngine where the steps will be simulated.
     * @param moves      fixed number of steps.
     */
    private void simulateSteps(GameEngine gameEngine, int moves) {
        for (int i = 0; i < moves; i++) {
            if (gameEngine.isFinished()) {
                break;
            }
            gameEngine.move();
        }
    }

    /**
     * Game simulation test:
     * {@link RevealingHider} gives perfect hint.
     * {@link NaiveCircleSearcher} follows.
     * {@link GameEngine#isFinished()} should now return true.
     */
    @Test
    void moveOnTreasure() {
        GameEngine gameEngine = new GameEngine(new NaiveCircleSearcher(), new RevealingHider(), new Coordinate(0, 0));
        gameEngine.init();
        simulateSteps(gameEngine, 2);
        assertTrue(gameEngine.isFinished());
        assertTrue(gameEngine.treasurePos.getX() == gameEngine.searcherPos.getX());
        assertTrue(gameEngine.treasurePos.getY() == gameEngine.searcherPos.getY());
    }

    /**
     * This tests the {@link GameEngine#located(List, Coordinate)} method.
     */
    @Test
    void bruteForceTest1() {
        GameEngine gameEngine = new GameEngine(new BruteForceSearcher(), new RevealingHider());
        gameEngine.init();
        simulateSteps(gameEngine, 44);
        assertFalse(gameEngine.isFinished());
        simulateSteps(gameEngine, 1);
        assertTrue(gameEngine.isFinished());
    }

    /**
     * The {@link RevealingHider} places the treasure.
     * The searcher, in one {@link SearchPathPrototype},
     * walks first ON the treasure,
     * the leaves it with a distance > 1.
     */
    @Test
    void moveOverTreasure1() {
        GameEngine gameEngine = new GameEngine(new MoveOverTreasure1Searcher(), new RevealingHider());
        gameEngine.init();
        simulateSteps(gameEngine, 2);
        assertTrue(gameEngine.isFinished());
    }

    /**
     * The {@link RevealingHider} places the treasure.
     * The searcher, in one {@link SearchPathPrototype},
     * walks OVER the treasure, but stops
     * at a distance > 1.
     */
    @Test
    void moveOverTreasure2() {
        GameEngine gameEngine = new GameEngine(new MoveOverTreasure2Searcher(), new RevealingHider());
        gameEngine.init();
        simulateSteps(gameEngine, 2);
        assertTrue(gameEngine.isFinished());
    }

    /**
     * Here, the treasure spawns under the searcher.
     * Thus, the game is instantly finished.
     */
    @Test
    void spawnOnTreasure() {
        GameEngine gameEngine = new GameEngine(new StandingSearcher(), new InstantWinHider());
        gameEngine.init();
        assertTrue(gameEngine.isFinished());
    }

    /**
     * {@link GameEngine#located(List, Coordinate)} test.
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
            private Coordinate treasurePos = new Coordinate(1, 1);

            @Override
            public Point getTreasureLocation() {
                return JTSUtils.createPoint(treasurePos.x, treasurePos.y);
            }

            @Override
            public void init(Point searcherStartPosition) {
            }

            @Override
            public Hint move(SearchPath searchPath) {
                return new CircleHint(new Coordinate(0, 2), 2);
            }
        });
        gameEngine.init();
        simulateSteps(gameEngine, 2);
        assertTrue(gameEngine.isFinished());
    }
}