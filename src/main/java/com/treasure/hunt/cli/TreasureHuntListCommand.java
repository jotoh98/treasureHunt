package com.treasure.hunt.cli;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.ReflectionUtils;
import picocli.CommandLine;

import java.util.Set;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "list",
        description = "List all possible Searcher, Hider and GameEngines"
)
public class TreasureHuntListCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;

    @Override
    public Integer call() throws Exception {
        Set<Class<? extends Searcher>> allSearchers = ReflectionUtils.getAllSearchers();
        System.out.println("\nThe following Searchers are available...");
        allSearchers.forEach(aClass -> System.out.printf("%s(%s)%n", aClass.getCanonicalName(), ReflectionUtils.genericName(aClass)));

        Set<Class<? extends Hider>> allHiders = ReflectionUtils.getAllHiders();
        System.out.println("\nThe following Hiders are available...");
        allHiders.forEach(aClass -> System.out.printf("%s(%s)%n", aClass.getCanonicalName(), ReflectionUtils.genericName(aClass)));

        Set<Class<? extends GameEngine>> allGameEngines = ReflectionUtils.getAllGameEngines();
        System.out.println("\nThe following GameEngines are available...");
        allGameEngines.forEach(aClass -> System.out.printf("%s%n", aClass.getCanonicalName()));
        return 0;
    }
}
