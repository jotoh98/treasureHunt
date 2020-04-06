package com.treasure.hunt.view.widget;

import com.treasure.hunt.analysis.StatisticObject;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.service.settings.SettingsService;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import lombok.extern.slf4j.Slf4j;

/**
 * @author axel1200, Daniel
 */
@Slf4j
public class StatisticsWidgetController {
    public TableView<StatisticObject> statisticsTable;
    public TableColumn<StatisticObject, StatisticObject> nameColumn;
    public TableColumn<StatisticObject, String> valueColumn;

    private ObjectProperty<GameManager> gameManager;

    public void initialize() {
    }

    public void init(ObjectProperty<GameManager> gameManager) {
        this.gameManager = gameManager;

        initializeColumnValueFactory();
        fillTable();
    }

    private void fillTable() {
        InvalidationListener statisticsChangeListener = (observable) -> statisticsTable.getItems().setAll(gameManager.get().getStatistics().get());

        gameManager.addListener(observable -> {
            statisticsChangeListener.invalidated(null);
            gameManager.get().getStatistics()
                    .addListener(statisticsChangeListener);
        });

        gameManager.get().getStatistics()
                .addListener(statisticsChangeListener);

        statisticsChangeListener.invalidated(null);
    }

    private void initializeColumnValueFactory() {
        nameColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));

        valueColumn.setCellValueFactory(param -> new SimpleStringProperty(
                SettingsService.getInstance().getSettings().round(param.getValue().getValue().doubleValue())
        ));

        nameColumn.setCellFactory(column ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(StatisticObject item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setTooltip(null);
                            return;
                        }
                        setText(item.getStatisticInfo().getName());
                        setTooltip(new Tooltip(item.getStatisticInfo().getDescription()));
                    }
                });
    }
}

