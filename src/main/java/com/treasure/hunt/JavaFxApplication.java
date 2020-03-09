package com.treasure.hunt;

import com.treasure.hunt.view.SplashScreenLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

/**
 * The javafx application main class.
 * Sets properties for the application as well as launching the stage.
 *
 * @author jotoh
 */
@Slf4j
public class JavaFxApplication extends Application {
    static {
        System.getProperties().setProperty("-Xdock:name", "TreasureHunt");
    }

    /**
     * Main application invoker.
     *
     * @param args cli args
     */
    public static void main(String[] args) {
        setTaskBarIcon();
        launch(args);
    }

    /**
     * Set the taskbar icon for the app.
     */
    private static void setTaskBarIcon() {
        Image image = Toolkit.getDefaultToolkit().getImage(JavaFxApplication.class.getResource("/images/icon.png"));
        try {
            Taskbar.getTaskbar().setIconImage(image);
        } catch (Exception e) {
            log.info("This platform seems to not support taskbar icon image");
        }
    }

    /**
     * Start the javafx application.
     *
     * @param stage the stage associated with the current javafx app
     * @throws Exception if main layout could not be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("TreasureHunt");

        setStageIcon(stage);
        scene.getStylesheets().add(getClass().getResource("/layout/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        //We are idiots we put 1.5 seconds more loading time so you can see your awesome splash screen
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                stage.show();
                SplashScreenLoader.splashScreen.hide();
            });
        });
    }

    /**
     * Set the icon for the javafx stage.
     *
     * @param stage the stage associated with the current javafx app
     */
    private void setStageIcon(Stage stage) {
        try {
            stage.getIcons().add(new javafx.scene.image.Image(JavaFxApplication.class.getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            log.info("This platform seems to not support stage icon image");
        }
    }
}
