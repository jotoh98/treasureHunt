package com.treasure.hunt.game;

import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

/**
 * This test evaluates the functionality of the early exit feature.
 *
 * @author jotoh
 */
@Slf4j
class EarlyExitTest {

    /**
     * This tests the overall functionality of the early exit.
     */
    @SneakyThrows
    @Test
    void functionality() {
        PreferenceService.getInstance().putPreference(PreferenceService.EARLY_EXIT_AMOUNT, 3);
        PreferenceService.getInstance().putPreference(PreferenceService.EARLY_EXIT_RADIUS, 3.0);

        final GameManager gameManager = new GameManager(EmptySearcher.class, EmptyHider.class, GameEngine.class);
        gameManager.init();

        gameManager.next();
        Assertions.assertFalse(gameManager.earlyExit());
        gameManager.next();
        Assertions.assertFalse(gameManager.earlyExit());
        gameManager.next();
        Assertions.assertTrue(gameManager.earlyExit());
    }

    /**
     * This test demonstrates, that a EARLY_EXIT_AMOUNT &le; 1 results in no early exit.
     */
    @SneakyThrows
    @Test
    void smallAmount() {
        PreferenceService.getInstance().putPreference(PreferenceService.EARLY_EXIT_AMOUNT, 1);
        PreferenceService.getInstance().putPreference(PreferenceService.EARLY_EXIT_RADIUS, 500);

        final GameManager gameManager = new GameManager(EmptySearcher.class, EmptyHider.class, GameEngine.class);
        gameManager.init();
        while (!gameManager.getFinishedProperty().get()) {
            gameManager.next();
            Assertions.assertFalse(gameManager.earlyExit());
        }
    }

    /**
     * This test demonstrates, that a small EARLY_EXIT_RADIUS results in no early exit.
     */
    @SneakyThrows
    @Test
    void smallRadius() {
        PreferenceService.getInstance().putPreference(PreferenceService.EARLY_EXIT_AMOUNT, 2);
        PreferenceService.getInstance().putPreference(PreferenceService.EARLY_EXIT_RADIUS, 1e-10);

        final GameManager gameManager = new GameManager(EmptySearcher.class, EmptyHider.class, GameEngine.class);
        gameManager.init();
        while (!gameManager.getFinishedProperty().get()) {
            gameManager.next();
            Assertions.assertFalse(gameManager.earlyExit());
        }
    }

    /**
     * This {@link Searcher} does only go one LE to EAST.
     */
    public static class EmptySearcher implements Searcher<Hint> {
        private static Vector2D translate = Vector2D.create(1, 0);
        Point last;

        /**
         * {@inheritDoc}
         */
        @Override
        public void init(final Point searcherStartPosition) {
            last = searcherStartPosition;
        }

        /**
         * @return {@link EmptySearcher#move(Hint)} with argument {@code null}.
         */
        @Override
        public SearchPath move() {
            return move(null);
        }

        /**
         * @param hint the hint, the {@link Hider} gave last.
         * @return A {@link SearchPath}, in which the {@link Searcher} moves only one LE to EAST.
         */
        @Override
        public SearchPath move(final Hint hint) {
            last = JTSUtils.createPoint(translate.translate(last.getCoordinate()));
            return new SearchPath(last);
        }
    }

    /**
     * A type of {@link Hider}, placing the treasore on {@code (10,0)} and does only give empty {@link Hint} objects.
     */
    public static class EmptyHider implements Hider<Hint> {
        Point treasureLocation;

        /**
         * Places the treasure on {@code (10,0}.
         *
         * @param searcherStartPosition the {@link com.treasure.hunt.strategy.searcher.Searcher} starting position,
         */
        @Override
        public void init(final Point searcherStartPosition) {
            treasureLocation = JTSUtils.createPoint(10, 0);
        }

        /**
         * @param searchPath the {@link SearchPath}, the {@link Searcher} did last
         * @return a valid {@link com.treasure.hunt.jts.geom.HalfPlane}, parallel to the y-axis.
         */
        @Override
        public Hint move(final SearchPath searchPath) {
            if (searchPath.getLastPoint().getX() < treasureLocation.getX()) {
                return new HalfPlaneHint(searchPath.getLastPoint().getCoordinate(),
                        new Coordinate(searchPath.getLastPoint().getCoordinate().x, searchPath.getLastPoint().getCoordinate().y - 1));
            } else {
                return new HalfPlaneHint(searchPath.getLastPoint().getCoordinate(),
                        new Coordinate(searchPath.getLastPoint().getCoordinate().x, searchPath.getLastPoint().getCoordinate().y + 1));
            }
        }

        /**
         * @return a {@link Point} describing the treasure location on {@code (10,0)}.
         */
        @Override
        public Point getTreasureLocation() {
            return treasureLocation;
        }
    }

}