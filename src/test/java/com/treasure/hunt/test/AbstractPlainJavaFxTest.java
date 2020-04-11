package com.treasure.hunt.test;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
/**
 * Stolen from Downlord's FAF client
 */
public abstract class AbstractPlainJavaFxTest extends ApplicationTest {

    private final Pane root;
    private Scene scene;
    private Stage stage;

    public AbstractPlainJavaFxTest() {
        root = new Pane();
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> log.error("Unresolved Throwable in none junit thread, please resolve even if test does not fail", e));
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        scene = createScene(stage);
        stage.setScene(scene);

        if (showStage()) {
            stage.show();
        }
    }

    protected Scene createScene(Stage stage) {
        return new Scene(getRoot(), 1, 1);
    }

    protected boolean showStage() {
        return true;
    }

    protected Pane getRoot() {
        return root;
    }

    protected Scene getScene() {
        return scene;
    }

    protected Stage getStage() {
        return stage;
    }

    protected void loadFxml(String fileName, Callback<Class<?>, Object> controllerFactory) throws IOException {

        @Data
        class ExceptionWrapper {
            private Exception loadException;
        }

        ExceptionWrapper loadExceptionWrapper = new ExceptionWrapper();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource(fileName));
        loader.setControllerFactory(controllerFactory);
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                loader.load();
            } catch (Exception e) {
                loadExceptionWrapper.setLoadException(e);
            }
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (loadExceptionWrapper.getLoadException() != null) {
            throw new RuntimeException("Loading fxm failed", loadExceptionWrapper.getLoadException());
        }
    }

    protected String getThemeFile(String file) {
        return String.format("/%s", file);
    }
}
