package com.treasure.hunt.view.widget;

import com.treasure.hunt.analysis.StatisticObject;
import com.treasure.hunt.game.GameManager;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatisticsWidgetController {

    public TableView<StatisticObject> statisticsTable;
    public TableColumn<StatisticObject, String> nameColumn;
    public TableColumn<StatisticObject, String> valueColumn;

    private ObjectProperty<GameManager> gameManager;

    public void initialize() {
    }

    public void init(ObjectProperty<GameManager> gameManager, Label logLabel) {
        this.gameManager = gameManager;

        initializeColumnValueFactory();
        fillTable();
    }

    private void fillTable() {
        InvalidationListener statisticsChangeListener = (observable) -> statisticsTable.getItems().setAll(gameManager.get().getStatistics().get());

        gameManager.addListener(observable ->
                gameManager.get().getStatistics()
                        .addListener(statisticsChangeListener));

        gameManager.get().getStatistics()
                .addListener(statisticsChangeListener);

        statisticsChangeListener.invalidated(null);
    }

    private void initializeColumnValueFactory() {
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTitle()));
        valueColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().toString()));
    }
}

