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
        setTaskBarIcon();
        launch(args);
    }

    private static void setTaskBarIcon() {
        Image image = Toolkit.getDefaultToolkit().getImage(JavaFxApplication.class.getResource("/images/icon.png"));
        try {
            Taskbar.getTaskbar().setIconImage(image);
        } catch (Exception e) {
            log.info("This platform seems to not support taskbar icon image");
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("TreasureHunt");

        setStageIcon(stage);
        scene.getStylesheets().add(getClass().getResource("/layout/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> Platform.exit());
    }

    private void setStageIcon(Stage stage) {
        try {
            stage.getIcons().add(new javafx.scene.image.Image(JavaFxApplication.class.getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            log.info("This platform seems to not support stage icon image");
        }
    }
}
