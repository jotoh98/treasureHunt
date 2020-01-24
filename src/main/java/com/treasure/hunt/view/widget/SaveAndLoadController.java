package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.service.io.FileService;
import com.treasure.hunt.service.io.SeriesService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.EventBusUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * @author axel1200
 */
@Slf4j
public class SaveAndLoadController {

    public ComboBox<Class<? extends Searcher>> searcherList;
    public ComboBox<Class<? extends Hider>> hiderList;
    public ComboBox<Class<? extends GameEngine>> gameEngineList;

    public Spinner<Integer> roundSpinner;
    public ProgressIndicator progressIndicator;
    public Button saveButton;
    public Button runMultipleButton;
    private ObjectProperty<GameManager> gameManager;

    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 100000, 1000, 100);
        roundSpinner.setEditable(true);
        roundSpinner.setValueFactory(valueFactory);
        progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());
    }

    public void init(ObjectProperty<GameManager> gameManager, ComboBox<Class<? extends Searcher>> searcherList, ComboBox<Class<? extends Hider>> hiderList, ComboBox<Class<? extends GameEngine>> gameEngineList) {
        this.gameManager = gameManager;
        saveButton.disableProperty().bind(gameManager.isNull());
        this.searcherList = searcherList;
        this.hiderList = hiderList;
        this.gameEngineList = gameEngineList;
        runMultipleButton.disableProperty().bind(searcherList.getSelectionModel().selectedItemProperty().isNull()
                .or(hiderList.getSelectionModel().selectedItemProperty().isNull())
                .or(gameEngineList.getSelectionModel().selectedItemProperty().isNull())
        );
        roundSpinner.disableProperty().bind(searcherList.getSelectionModel().selectedItemProperty().isNull()
                .or(hiderList.getSelectionModel().selectedItemProperty().isNull())
                .or(gameEngineList.getSelectionModel().selectedItemProperty().isNull())
        );

    }

    public void save(ActionEvent actionEvent) {
        FileService.getInstance().saveGameManager(gameManager.get());
    }

    public void load(ActionEvent actionEvent) {
        FileService.getInstance().loadGameManager();
    }

    public void onSeriesRun(ActionEvent actionEvent) {
        progressIndicator.setVisible(true);
        try {
            Class<? extends GameEngine> selectedGameEngine = gameEngineList.getSelectionModel().getSelectedItem();
            Class<? extends Searcher> selectedSearcher = searcherList.getSelectionModel().getSelectedItem();
            Class<? extends Hider> selectedHider = hiderList.getSelectionModel().getSelectedItem();
            GameManager newGameManager = new GameManager(selectedSearcher, selectedHider, selectedGameEngine);
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

