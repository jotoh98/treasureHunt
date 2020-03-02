package com.treasure.hunt.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "treasure",
        description = "this command can be used to access the treasure CLI.",
        subcommands = {
                TreasureHuntListCommand.class,
                TreasureHuntRunSeriesCommand.class
        }
)
public class TreasureHuntCommand {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;
}
