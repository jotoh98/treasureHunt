package com.treasure.hunt.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncUtils {
    public static final ExecutorService EXECUTOR_SERVICE;

    static {
        EXECUTOR_SERVICE = newExhaustingThreadPoolExecutor();
    }

    @NotNull
    public static ExecutorService newExhaustingThreadPoolExecutor() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }
}
