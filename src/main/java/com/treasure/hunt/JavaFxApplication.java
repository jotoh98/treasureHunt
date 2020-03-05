package com.treasure.hunt;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

/**
 * @author jotoh
 */
@Slf4j
public class JavaFxApplication extends Application {
    static {
        System.getProperties().setProperty("-Xdock:name", "TreasureHunt");
    }

    public static void main(String[] args) {
        Image image = Toolkit.getDefaultToolkit().getImage(JavaFxApplication.class.getResource("/images/icon.png"));
        Taskbar.getTaskbar().setIconImage(image);
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("TreasureHunt");
        scene.getStylesheets().add(getClass().getResource("/layout/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> Platform.exit());
    }
}
