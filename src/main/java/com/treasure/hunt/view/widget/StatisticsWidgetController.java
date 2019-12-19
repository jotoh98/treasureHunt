package com.treasure.hunt.view.widget;

import com.treasure.hunt.analysis.StatisticObject;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.utils.JsonFileUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class StatisticsWidgetController {

    public TableView<StatisticObject>statisticsTable;
    public TableColumn<StatisticObject,String> nameColumn;
    public TableColumn<StatisticObject,String> valueColumn;

    private ObjectProperty<GameManager> gameManager;
    private Label logLabel;

    public void initialize(){
    }

    public void init(ObjectProperty<GameManager> gameManager, Label logLabel) {
        this.gameManager = gameManager;
        this.logLabel = logLabel;

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

