package com.treasure.hunt.cli;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.service.io.SeriesService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


@CommandLine.Command(
        name = "run",
        description = "runs a series"
)
public class TreasureHuntRunSeriesCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;

    @CommandLine.Option(names = {"-s", "--searcher"}, description = "provide the Searcher Class used for the series", required = true)
    private Class<? extends Searcher> searcher;

    @CommandLine.Option(names = {"-hi", "--hider"}, description = "provide the Hider Class used for the series", required = true)
    private Class<? extends Hider> hider;

    @CommandLine.Option(names = {"-g", "--gameEngine"}, description = "provide the GameEngine Class used for the series", required = true)
    private Class<? extends GameEngine> gameEngine;

    @CommandLine.Option(names = {"-r", "--round"}, description = "how many runs should be made in the series", required = true)
    private Integer rounds;

    @CommandLine.Option(names = {"-f", "--file"}, description = "output file location")
    private Path path;

    @CommandLine.Option(names = {"-sI", "--sameInit"}, description = "same init this mostly results in same treasure location and start location")
    private boolean sameInit;

    @CommandLine.Option(names = {"-wG", "--withOutGameManger"}, description = "Whether to include the runs in the hunts file")
    private boolean withOutGamemanger = false;

    @Override
    /**
     * Uses a progress bar See here for more info @Link{https://tongfei.me/progressbar/}
     */
    public Integer call() throws Exception {
        if (path == null) {
            path = Path.of("./saved.hunts");
        }
        GameManager gameManager = new GameManager(searcher, hider, gameEngine);
        if (sameInit) {
            gameManager.init();
        }
        ProgressBar progressBar = new ProgressBarBuilder()
                .setTaskName("Traversing")
                .setUnit("%", 1)
                .setInitialMax(100)
                .build();
        AtomicInteger lastProgress = new AtomicInteger();
        SeriesService.getInstance()
                .runSeriesAndSaveToFile(rounds, gameManager, progress -> lastProgress.set(updateProgress(progress, progressBar, lastProgress.get())), path.toFile(), sameInit, !withOutGamemanger);
        progressBar.close();
        return 0;
    }

    private int updateProgress(Double progress, ProgressBar progressBar, int lastProgress) {
        int newProgress = (int) (progress * 100);
        if (newProgress != lastProgress) {
            progressBar.stepTo(newProgress);
            return newProgress;
        }
        return lastProgress;
    }
}
