package com.treasure.hunt;

import com.treasure.hunt.view.SplashScreenLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * @author jotoh
 */
@Slf4j
public class JavaFxApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

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
}
