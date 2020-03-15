package com.treasure.hunt;

import com.treasure.hunt.service.settings.Session;
import com.treasure.hunt.service.settings.SettingsService;
import com.treasure.hunt.view.MainController;
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

    @Override
    public void start(Stage stage) throws Exception {

        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("TreasureHunt");

        setStageIcon(stage);
        scene.getStylesheets().add(getClass().getResource("/layout/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            MainController mainController = fxmlLoader.getController();
            mainController.saveSession();
            saveSession(stage);
            Platform.exit();
        });

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                loadSession(stage);
                stage.show();
                SplashScreenLoader.splashScreen.hide();
            });
        });
    }

    private void setStageIcon(Stage stage) {
        try {
            stage.getIcons().add(new javafx.scene.image.Image(JavaFxApplication.class.getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            log.info("This platform seems to not support stage icon image");
        }
    }

    private void loadSession(Stage stage) {
        Session session = SettingsService.getInstance().getSession();
        stage.setX(session.getWindowLeft());
        stage.setY(session.getWindowTop());
        stage.setWidth(session.getWindowWidth());
        stage.setHeight(session.getWindowHeight());
        stage.setFullScreen(session.isFullscreen());
    }

    private void saveSession(Stage stage) {
        Session session = SettingsService.getInstance().getSession();
        session.setWindowLeft(stage.getX());
        session.setWindowTop(stage.getY());
        session.setWindowWidth(stage.getWidth());
        session.setWindowHeight(stage.getHeight());
        session.setFullscreen(stage.isFullScreen());
        SettingsService.getInstance().saveSession();
    }

}
