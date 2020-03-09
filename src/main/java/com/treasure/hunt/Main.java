package com.treasure.hunt;

import com.sun.javafx.application.LauncherImpl;
import com.treasure.hunt.view.SplashScreenLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * Main entry class for the program.
 * Utilised as a prevention for the java package constraints coming with javafx.
 *
 * @author axel12
 */
@Slf4j
public class Main {
    /**
     * Starts the launcher for the application.
     * The main application as well as the splash screen loader are associated.
     *
     * @param args cli arguments
     * @see JavaFxApplication
     * @see SplashScreenLoader
     */
    public static void main(String[] args) {
        log.info("Starting Javafx UI application");
        LauncherImpl.launchApplication(JavaFxApplication.class, SplashScreenLoader.class, args);
    }
}
