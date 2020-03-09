package com.treasure.hunt.view;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Controller for the splash screen.
 */
public class SplashScreenLoader extends Preloader {

    /**
     * Splash screen stage.
     */
    public static Stage splashScreen;

    /**
     * Start the splash screen.
     *
     * @param stage splash screen stage
     * @throws IOException throws exception if splash screen layout could not be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.UNDECORATED);
        splashScreen = stage;
        splashScreen.setScene(createScene());
        splashScreen.show();
    }

    /**
     * Create the splash screen scene.
     *
     * @return scene containing the splash screen
     * @throws IOException throws exception if splash screen layout could not be loaded
     */
    public Scene createScene() throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/splash.fxml"));
        Parent load = fxmlLoader.load();
        return new Scene(load, 450, 300);
    }

}