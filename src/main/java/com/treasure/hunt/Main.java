package com.treasure.hunt;

import com.sun.javafx.application.LauncherImpl;
import com.treasure.hunt.view.SplashScreenLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * @author axel12
 */
@Slf4j
public class Main {
    public static void main(String[] args) {
        log.info("Starting Javafx UI application");
        LauncherImpl.launchApplication(JavaFxApplication.class, SplashScreenLoader.class, args);
    }
}
