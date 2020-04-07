package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hider.impl.InstantWinHider;
import com.treasure.hunt.strategy.hider.impl.RevealingHider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.impl.*;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the {@link GameEngine}.
 *
 * @author dorianreineccius
 */
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
        GameEngine gameEngine = new GameEngine(new NaiveCircleSearcher(), new RevealingHider(), JTSUtils.createPoint(0, 0));
        gameEngine.init();
        simulateSteps(gameEngine, 2);
        assertTrue(gameEngine.isFinished());
        assertTrue(gameEngine.treasurePos.equalsTopo(gameEngine.searcherPos));
    }

    /**
     * This tests the {@link GameEngine#located(SearchPath, Point)} method.
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
     * The searcher, in one {@link SearchPath},
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
     * The searcher, in one {@link SearchPath},
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
     * {@link GameEngine#located(SearchPath, Point)} test.
     * In this test, the searcher moves <b>past</b> the treasure
     * with a minimum distance of 1.
     * searcher starts at (0,0) as usual.
     * treasure spawns at (1,1).
     * hider tells the point (2,0).
     */
    @Test
    void narrowMove() {
        GameEngine gameEngine = new GameEngine(new NaiveCircleSearcher(), new Hider() {

            private Point treasurePos = JTSUtils.createPoint(1, 1);

            @Override
            public Point getTreasureLocation() {
                return treasurePos;
            }

            @Override
            public void init(Point searcherStartPosition) {
            }

            @Override
            public Hint move(SearchPath moves) {
                return new CircleHint(JTSUtils.createPoint(0, 2), 2);
            }
        });
        gameEngine.init();
        simulateSteps(gameEngine, 2);
        assertTrue(gameEngine.isFinished());
    }

    /**
     * Tests {@link GameEngine#verifyHint(AngleHint, AngleHint, Point, Point)}
     */
    @Test
    void verifyHintTest1() {
        // current AngleHint must not be null
        assertThrows(IllegalArgumentException.class, () ->
                GameEngine.verifyHint(new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                        null, JTSUtils.createPoint(1, 1), JTSUtils.createPoint(0, 0)));
        assertThrows(IllegalArgumentException.class, () ->
                GameEngine.verifyHint(new CircleHint(new Coordinate(1, 1), 1),
                        null, JTSUtils.createPoint(1, 1), JTSUtils.createPoint(0, 0)));
    }

    /**
     * Tests {@link GameEngine#verifyHint(CircleHint, CircleHint, Point, Point)},
     * especially, whether the {@link CircleHint} contain the treasure
     */
    @Test
    void verifyCircleHintTest1() {
        GameEngine.verifyHint(null, new CircleHint(new Coordinate(0, 0), 1),
                JTSUtils.createPoint(1, 0), null);
        assertThrows(IllegalArgumentException.class, () ->
                GameEngine.verifyHint(null, new CircleHint(new Coordinate(0, 0), 1),
                        JTSUtils.createPoint(2, 0), null));
    }

    /**
     * Tests {@link GameEngine#verifyHint(CircleHint, CircleHint, Point, Point)},
     * especially, whether the previous {@link CircleHint} contain the current {@link CircleHint}.
     */
    @Test
    void verifyCircleHintTest2() {
        GameEngine.verifyHint(new CircleHint(new Coordinate(0, 0), 2),
                new CircleHint(new Coordinate(0, 0), 1), JTSUtils.createPoint(0, 0), null);
        GameEngine.verifyHint(new CircleHint(new Coordinate(0, 0), 1),
                new CircleHint(new Coordinate(0, 0), 1), JTSUtils.createPoint(0, 0), null);
        GameEngine.verifyHint(new CircleHint(new Coordinate(0, 0), 4),
                new CircleHint(new Coordinate(1, 0), 1), JTSUtils.createPoint(1, 0), null);
        // small circle cannot contain a bigger one
        assertThrows(IllegalArgumentException.class, () ->
                GameEngine.verifyHint(new CircleHint(new Coordinate(0, 0), 1),
                        new CircleHint(new Coordinate(0, 0), 2), JTSUtils.createPoint(0, 0), null));
        // circles disjoint
        assertThrows(IllegalArgumentException.class, () ->
                GameEngine.verifyHint(new CircleHint(new Coordinate(0, 0), 1),
                        new CircleHint(new Coordinate(2, 0), 1), JTSUtils.createPoint(2, 0), null));
        // circle not completely containing.
        assertThrows(IllegalArgumentException.class, () ->
                GameEngine.verifyHint(new CircleHint(new Coordinate(0, 0), 2),
                        new CircleHint(new Coordinate(2, 0), 1), JTSUtils.createPoint(2, 0), null));
    }

    /**
     * Tests {@link GameEngine#verifyHint(AngleHint, AngleHint, Point, Point)},
     * especially, whether it lies on the {@link com.treasure.hunt.strategy.searcher.Searcher}s position.
     */
    @Test
    void verifyAngleHintTest1() {
        GameEngine.verifyHint(null,
                new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                JTSUtils.createPoint(1, 1), JTSUtils.createPoint(0, 0));
        assertThrows(IllegalArgumentException.class, () ->
                GameEngine.verifyHint(null,
                        new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                        JTSUtils.createPoint(1, 1), JTSUtils.createPoint(1, 1))
        );
    }

    /**
     * Tests {@link GameEngine#verifyHint(AngleHint, AngleHint, Point, Point)},
     * especially, whether the treasure lies in the given {@link AngleHint}.
     */
    @Test
    void verifyAngleHintTest2() {
        GameEngine.verifyHint(null,
                new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                JTSUtils.createPoint(1, 1), JTSUtils.createPoint(0, 0));
        GameEngine.verifyHint(null,
                new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                JTSUtils.createPoint(1, 0), JTSUtils.createPoint(0, 0));
        GameEngine.verifyHint(null,
                new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                JTSUtils.createPoint(0, 1), JTSUtils.createPoint(0, 0));
        GameEngine.verifyHint(null,
                new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                JTSUtils.createPoint(0, 0), JTSUtils.createPoint(0, 0));
        GameEngine.verifyHint(null,
                new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                JTSUtils.createPoint(0, 400), JTSUtils.createPoint(0, 0));
        assertThrows(IllegalArgumentException.class, () ->
                GameEngine.verifyHint(null,
                        new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                        JTSUtils.createPoint(-1, -1), JTSUtils.createPoint(1, 1))
        );
        assertThrows(IllegalArgumentException.class, () ->
                GameEngine.verifyHint(null,
                        new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                        JTSUtils.createPoint(-1, 0), JTSUtils.createPoint(1, 1))
        );
        assertThrows(IllegalArgumentException.class, () ->
                GameEngine.verifyHint(null,
                        new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                        JTSUtils.createPoint(0, -1), JTSUtils.createPoint(1, 1))
        );
        assertThrows(IllegalArgumentException.class, () ->
                GameEngine.verifyHint(null,
                        new AngleHint(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)),
                        JTSUtils.createPoint(-.1, 400), JTSUtils.createPoint(1, 1))
        );
    }
}
