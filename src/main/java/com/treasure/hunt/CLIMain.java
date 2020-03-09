package com.treasure.hunt;

import ch.qos.logback.classic.Level;
import com.treasure.hunt.cli.TreasureHuntCommand;
import picocli.CommandLine;

/**
 * Main class started when program runs in CLI Mode, just type -h and see how to use it.
 *
 * @author axel12
 */
public class CLIMain {
    public static void main(String[] args) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.OFF);
        CommandLine commandLine = new CommandLine(new TreasureHuntCommand());
        commandLine.execute(args);
    }
}
