package com.treasure.hunt.service;

import com.treasure.hunt.analysis.StatisticsWithIdsAndPath;
import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.impl.RandomAngleHintHider;
import com.treasure.hunt.strategy.searcher.impl.NaiveAngleSearcher;
import com.treasure.hunt.test.AbstractPlainJavaFxTest;
import com.treasure.hunt.utils.EventBusUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SeriesServiceTest extends AbstractPlainJavaFxTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testWriteSeries() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        testFolder.create();
        File file = testFolder.newFile("save.hunts");
        GameManager gameManager = new GameManager(NaiveAngleSearcher.class, RandomAngleHintHider.class, GameEngine.class);
        SeriesService.getInstance().runSeriesAndSaveToFile(100, gameManager, aDouble -> {
        }, file, false, true);
        CountDownLatch loadLatch = new CountDownLatch(1);
        AtomicReference<StatisticsWithIdsAndPath> statisticsWithIdsAndPath = new AtomicReference<>();
        EventBusUtils.STATISTICS_LOADED_EVENT.addListener(data -> {
            statisticsWithIdsAndPath.set(data);
            loadLatch.countDown();
        });
        SeriesService.getInstance().readStatistics(file.toPath());
        loadLatch.await(25, TimeUnit.SECONDS);
        assertNotNull(statisticsWithIdsAndPath.get());
        assertEquals(statisticsWithIdsAndPath.get().getFile(), file.toPath());
        assertEquals(statisticsWithIdsAndPath.get().getStatisticsWithIds().size(), 100);
    }

}