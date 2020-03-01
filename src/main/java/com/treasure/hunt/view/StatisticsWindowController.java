package com.treasure.hunt.view;

import com.treasure.hunt.analysis.StatisticObject;
import com.treasure.hunt.analysis.StatisticsWithId;
import com.treasure.hunt.analysis.StatisticsWithIdsAndPath;
import com.treasure.hunt.service.io.FileService;
import com.treasure.hunt.service.io.SeriesService;
import com.treasure.hunt.utils.EventBusUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
public class StatisticsWindowController {
    public TableView<StatisticsWithId> instanceStatisticsTableView;
    public TableView<HashMap.Entry<StatisticObject.StatisticInfo, List<StatisticObject>>> statisticsMeasuresTable;
    HashMap<StatisticObject.StatisticInfo, List<StatisticObject>> statisticsMeasureHashMap = new HashMap<>();
    private Path path;

    public void initialize() {
        statisticsMeasuresTableInit();
        instanceTableInit();
    }

    private void statisticsMeasuresTableInit() {
        TableColumn<HashMap.Entry<StatisticObject.StatisticInfo, List<StatisticObject>>, String> nameColumn = new TableColumn<>();
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKey().getName()));
        statisticsMeasuresTable.getColumns().add(nameColumn);
        nameColumn.setText("name");

        TableColumn<HashMap.Entry<StatisticObject.StatisticInfo, List<StatisticObject>>, Double> averageColumn = new TableColumn<>();
        averageColumn.setCellValueFactory(param -> {
            List<StatisticObject> value = param.getValue().getValue();
            return new SimpleObjectProperty<>(value.stream().map(StatisticObject::getValue)
                    .mapToDouble(valueOfStatistic -> (double) valueOfStatistic)
                    .average().getAsDouble());
        });
        averageColumn.setText("average");
        statisticsMeasuresTable.getColumns().add(averageColumn);

        TableColumn<HashMap.Entry<StatisticObject.StatisticInfo, List<StatisticObject>>, Double> minColumn = new TableColumn<>();
        minColumn.setCellValueFactory(param -> {
            List<StatisticObject> value = param.getValue().getValue();
            return new SimpleObjectProperty<>(value.stream().map(StatisticObject::getValue)
                    .mapToDouble(valueOfStatistic -> (double) valueOfStatistic)
                    .min().getAsDouble());
        });
        minColumn.setText("min");
        statisticsMeasuresTable.getColumns().add(minColumn);

        TableColumn<HashMap.Entry<StatisticObject.StatisticInfo, List<StatisticObject>>, Double> maxColumn = new TableColumn<>();
        maxColumn.setCellValueFactory(param -> {
            List<StatisticObject> value = param.getValue().getValue();
            return new SimpleObjectProperty<>(value.stream().map(StatisticObject::getValue)
                    .mapToDouble(valueOfStatistic -> (double) valueOfStatistic)
                    .max().getAsDouble());
        });
        maxColumn.setText("max");
        statisticsMeasuresTable.getColumns().add(maxColumn);
    }

    private void instanceTableInit() {
        TableColumn<StatisticsWithId, Integer> idColumn = new TableColumn<>();
        idColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getId()));
        instanceStatisticsTableView.getColumns().add(idColumn);
        idColumn.setText("id");

        instanceStatisticsTableView.setRowFactory(tv -> {
            TableRow<StatisticsWithId> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    StatisticsWithId rowData = row.getItem();
                    try {
                        InputStream inputStream = SeriesService.getInstance().getHuntFileWithIdAsStream(rowData.getId(), path);
                        FileService.getInstance().
                                readGameManagerFromStreamAndLoad(inputStream);
                    } catch (Exception e) {
                        log.info("Error loading hunt file from series", e);
                        EventBusUtils.LOG_LABEL_EVENT.trigger("Unable to load run");
                    }

                }
            });
            return row;
        });
    }

    private void addAdditionalColumnsToInstanceTable() {
        statisticsMeasureHashMap.keySet()
                .forEach(statisticInfo -> {
                    TableColumn statisticColumnWithOutType;
                    if (statisticInfo.getType() != Double.class) {
                        TableColumn<StatisticsWithId, String> statisticColumn = new TableColumn<>();
                        statisticColumn.setCellValueFactory(param -> {
                            StatisticsWithId value = param.getValue();
                            List<StatisticObject> statisticObjects = value.getStatisticObjects();
                            Optional<StatisticObject> first = statisticObjects.stream()
                                    .filter(statisticObject -> statisticObject.getStatisticInfo().equals(statisticInfo))
                                    .findFirst();
                            StatisticObject statisticObject = first.orElseThrow();
                            return new SimpleStringProperty(statisticObject.getValue().toString());
                        });
                        statisticColumnWithOutType = statisticColumn;
                    } else {
                        TableColumn<StatisticsWithId, Double> statisticColumn = new TableColumn<>();
                        statisticColumn.setCellValueFactory(param -> {
                            StatisticsWithId value = param.getValue();
                            List<StatisticObject> statisticObjects = value.getStatisticObjects();
                            Optional<StatisticObject> first = statisticObjects.stream()
                                    .filter(statisticObject -> statisticObject.getStatisticInfo().equals(statisticInfo))
                                    .findFirst();
                            StatisticObject statisticObject = first.orElseThrow();
                            return new SimpleObjectProperty<>((Double) statisticObject.getValue());
                        });
                        statisticColumnWithOutType = statisticColumn;
                    }

                    instanceStatisticsTableView.getColumns().add(statisticColumnWithOutType);
                    statisticColumnWithOutType.setText(statisticInfo.getName());
                });
    }

    public void init(StatisticsWithIdsAndPath statisticsWithIdsAndPath) {
        path = statisticsWithIdsAndPath.getFile();
        instanceStatisticsTableView.setItems(FXCollections.observableArrayList(statisticsWithIdsAndPath.getStatisticsWithIds()));

        statisticsWithIdsAndPath.getStatisticsWithIds().forEach(statisticsWithId -> statisticsWithId.getStatisticObjects()
                .forEach(statisticObject -> {
                    StatisticObject.StatisticInfo statisticInfo = statisticObject.getStatisticInfo();
                    if (!statisticsMeasureHashMap.containsKey(statisticInfo)) {
                        statisticsMeasureHashMap.put(statisticInfo, new ArrayList<>());
                    }
                    statisticsMeasureHashMap.get(statisticInfo).add(statisticObject);
                }));

        statisticsMeasuresTable.setItems(FXCollections.observableArrayList(statisticsMeasureHashMap.entrySet()));
        addAdditionalColumnsToInstanceTable();
    }
}
