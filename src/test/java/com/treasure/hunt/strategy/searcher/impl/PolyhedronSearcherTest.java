package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.impl.RandomAngleHintHider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

class PolyhedronSearcherTest {

    public static GameManager gameManager;

    @BeforeAll
    static void setup() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        gameManager = new GameManager(PolyhedronSearcher.class, RandomAngleHintHider.class, GameEngine.class);
    }

    @Test
    void firstTest() {
        for (int i = 0; i < 10000; i++) {
            gameManager.beat().thenAccept(result -> {
                Assertions.assertTrue(gameManager.isGameFinished());
                try {
                    setup();
                } catch (Exception ignored) {
                }
            });
        }

    }
}