package com.treasure.hunt.view;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class SplashScreenLoader extends Preloader {

    public static Stage splashScreen;

    @Override
    public void start(Stage stage) throws Exception {
        stage.initStyle(StageStyle.UNDECORATED);
        splashScreen = stage;
        splashScreen.setScene(createScene());
        splashScreen.show();
    }

    public Scene createScene() throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/splash.fxml"));
        return new Scene(fxmlLoader.load(), 600, 400);
    }

}