package com.treasure.hunt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainJavaFX extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent load = FXMLLoader.load(getClass().getResource("/layout/main.fxml"));
        Scene scene = new Scene(load);
        scene.getStylesheets().add("/layout/style.css");
        stage.setScene(scene);
        stage.show();
    }
}
