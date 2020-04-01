package com.treasure.hunt.game;

import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import java.util.Collections;
import java.util.List;

/**
 * This test evaluates the functionality of the early exit feature.
 */
@Slf4j
class EarlyExitTest {

    private static Vector2D translate = Vector2D.create(1, 0);

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
        gameManager.next();
        Assertions.assertTrue(gameManager.earlyExit());
    }

    /**
     * This test demonstrates, that a EARLY_EXIT_AMOUNT smaller than 2 results in no early exit.
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

    public static class EmptySearcher implements Searcher<Hint> {
        Point last;

        @Override
        public void init(final Point searcherStartPosition) {
            last = searcherStartPosition;
        }

        @Override
        public SearchPath move() {
            return move(null);
        }

        @Override
        public SearchPath move(final Hint hint) {
            last = JTSUtils.createPoint(translate.translate(last.getCoordinate()));
            return new SearchPath(last);
        }
    }

    public static class EmptyHider implements Hider<Hint> {
        @Override
        public void init(final Point searcherStartPosition) {
        }

        @Override
        public Hint move(final SearchPath searchPath) {
            return new Hint() {
                @Override
                public List<GeometryItem<?>> getGeometryItems() {
                    return Collections.emptyList();
                }
            };
        }

        @Override
        public Point getTreasureLocation() {
            return JTSUtils.createPoint(10, 0);
        }
    }

}