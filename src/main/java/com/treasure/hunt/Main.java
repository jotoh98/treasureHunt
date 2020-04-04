package com.treasure.hunt;

import com.sun.javafx.application.LauncherImpl;
import com.treasure.hunt.view.SplashScreenLoader;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

/**
 * @author axel12
 */
@Slf4j
public class Main {

    public static void main(String[] args) {
        log.info("Starting Javafx UI application");
        setTaskBarIcon();
        LauncherImpl.launchApplication(JavaFxApplication.class, SplashScreenLoader.class, args);
    }

    private static void setTaskBarIcon() {
        Image image = Toolkit.getDefaultToolkit().getImage(JavaFxApplication.class.getResource("/images/icon.png"));
        try {
            Taskbar.getTaskbar().setIconImage(image);
        } catch (Exception e) {
            log.info("This platform seems to not support taskbar icon image");
        }
    }
}
