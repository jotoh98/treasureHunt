package com.treasure.hunt.game;

import com.treasure.hunt.service.preferences.PreferenceService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of the {@link GameManager#slowApproachExit()} method.
 */
class TreasureApproachTest {

    /**
     * We test the overall functionality of treasure approach early exit.
     * At first, we have less steps than since demands. Therefore, {@code false} should be asserted. The distance (1.1) is higher than
     * the steps size (1.0) therefore {@code true} is asserted.
     */
    @SneakyThrows
    @Test
    void functionality() {
        PreferenceService.getInstance().putPreference(PreferenceService.TREASURE_APPROACH_DISTANCE, 1.1);
        PreferenceService.getInstance().putPreference(PreferenceService.TREASURE_APPROACH_SINCE, 1);

        final GameManager gameManager = new GameManager(EarlyExitTest.EmptySearcher.class, EarlyExitTest.EmptyHider.class, GameEngine.class);

        gameManager.init();

        Assertions.assertFalse(gameManager.slowApproachExit());

        gameManager.next();

        Assertions.assertTrue(gameManager.slowApproachExit());
    }

    /**
     * Here, we run two length units in two steps.
     * We demand, that the searcher runs 3 length units in this amount of steps, therefore an early exit is met.
     */
    @SneakyThrows
    @Test
    void catches() {
        PreferenceService.getInstance().putPreference(PreferenceService.TREASURE_APPROACH_DISTANCE, 3);
        PreferenceService.getInstance().putPreference(PreferenceService.TREASURE_APPROACH_SINCE, 2);

        final GameManager gameManager = new GameManager(EarlyExitTest.EmptySearcher.class, EarlyExitTest.EmptyHider.class, GameEngine.class);

        gameManager.init();

        gameManager.next();
        gameManager.next();

        Assertions.assertTrue(gameManager.slowApproachExit());

    }

    /**
     * We test, if a distance too small for the steps approach doesn't result in an early exit.
     */
    @SneakyThrows
    @Test
    void smallDistance() {
        PreferenceService.getInstance().putPreference(PreferenceService.TREASURE_APPROACH_DISTANCE, .5);
        PreferenceService.getInstance().putPreference(PreferenceService.TREASURE_APPROACH_SINCE, 1);

        final GameManager gameManager = new GameManager(EarlyExitTest.EmptySearcher.class, EarlyExitTest.EmptyHider.class, GameEngine.class);

        gameManager.init();

        gameManager.next();

        Assertions.assertFalse(gameManager.slowApproachExit());
    }

}