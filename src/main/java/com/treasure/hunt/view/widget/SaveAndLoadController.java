package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.io.FileService;
import com.treasure.hunt.service.SeriesService;
import com.treasure.hunt.utils.EventBusUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * @author axel1200
 */
@Slf4j
public class SaveAndLoadController {

    public Spinner<Integer> roundSpinner;
    public ProgressIndicator progressIndicator;
    private ObjectProperty<GameManager> gameManager;

    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 100000, 1000, 100);
        roundSpinner.setValueFactory(valueFactory);
        progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());
    }

    public void init(ObjectProperty<GameManager> gameManager) {
        this.gameManager = gameManager;
    }

    public void save(ActionEvent actionEvent) {
        FileService.getInstance().saveGameManager(gameManager.get());
    }

    public void load(ActionEvent actionEvent) {
        FileService.getInstance().loadGameManager();
    }

    public void onSeriesRun(ActionEvent actionEvent) {
        progressIndicator.setVisible(true);
        GameManager gameManager = this.gameManager.get();
        try {
            GameManager newGameManager = new GameManager(gameManager.getSearcherClass(), gameManager.getHiderClass(), gameManager.getGameEngineClass());
            CompletableFuture.runAsync(() -> SeriesService.getInstance().runSeries(roundSpinner.getValue(), newGameManager, aDouble -> Platform.runLater(() -> progressIndicator.setProgress(aDouble))))
                    .exceptionally(throwable -> {
                        log.error("Game Series run failed", throwable);
                        return null;
                    })
                    .thenRun(() -> Platform.runLater(() -> progressIndicator.setVisible(false)));
        } catch (Exception e) {
            EventBusUtils.LOG_LABEL_EVENT.trigger("Could not create new GameManager.");
            log.error("Could not create new GameManager.", e);
        }
    }

    public void onSeriesLoad() {
        SeriesService.getInstance().readStatistics();
    }
}

