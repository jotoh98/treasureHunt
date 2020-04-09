package com.treasure.hunt.game;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.impl.BruteForceSearcher;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import static org.junit.Assert.assertTrue;

/**
 * Tests of {@link GameEngine#cutSearchPath(SearchPath, Point)}
 *
 * @author Dorian Reineccius
 */
public class CutTests {
    @Test
    public void cutTest1() {
        GameEngine gameEngine = new GameEngine(new BruteForceSearcher(), new Hider() {
            Point treasureLocation = JTSUtils.createPoint(-10, 0);

            /**
             * Nothing happens here.
             */
            @Override
            public void init(final Point searcherStartPosition) {
            }

            /**
             * @param searchPath the {@link SearchPath}, the {@link com.treasure.hunt.strategy.searcher.Searcher} did last
             * @return a {@link CircleHint} having the treasure in the center and a radius of 1.
             */
            @Override
            public Hint move(final SearchPath searchPath) {
                return new CircleHint(treasureLocation, 0);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Point getTreasureLocation() {
                return treasureLocation;
            }
        }, JTSUtils.createPoint(0, 0));
        gameEngine.init();
        for (int i = 0; i < 9; i++) {
            gameEngine.move();
        }
        SearchPath lastSearchPath = gameEngine.move().getSearchPath();
        assertTrue(gameEngine.finished);
        assertTrue(lastSearchPath.getPoints().get(0).equalsExact(JTSUtils.createPoint(-9, -9), 0));
        assertTrue("lastSearchPath.getPoints().get(1) expected to be (-9, 0), but was " + lastSearchPath.getPoints().get(1), lastSearchPath.getPoints().get(1).equalsExact(JTSUtils.createPoint(-9, 0), 0));
    }
}
