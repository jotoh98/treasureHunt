package com.treasure.hunt.view;

import com.opencsv.CSVWriter;
import com.treasure.hunt.analysis.StatisticObject;
import com.treasure.hunt.analysis.StatisticsWithId;
import com.treasure.hunt.analysis.StatisticsWithIdsAndPath;
import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.service.io.FileService;
import com.treasure.hunt.service.io.SeriesService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.EventBusUtils;
import com.treasure.hunt.utils.ListUtils;
import com.treasure.hunt.view.plot.PlotController;
import com.treasure.hunt.view.plot.PlotSettingsController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class StatisticTableController {
    public TableView<StatisticsWithId> instanceStatisticsTableView;
    public TableView<HashMap.Entry<StatisticObject.StatisticInfo, List<StatisticObject>>> statisticsMeasuresTable;
    public Button runMultipleButton;
    public ProgressIndicator progressIndicator;
    public Spinner<Integer> roundSpinner;
    public ComboBox<Class<? extends Searcher>> searcherList;
    public ComboBox<Class<? extends Hider>> hiderList;
    public ComboBox<Class<? extends GameEngine>> gameEngineList;
    public Spinner<Integer> maxStepsSpinner;
    HashMap<StatisticObject.StatisticInfo, List<StatisticObject>> statisticsMeasureHashMap = new HashMap<>();
    private Path path;
    private ObjectProperty<GameManager> gameManager;

    public void initialize() {
        statisticsMeasuresTableInit();
        instanceTableInit();

        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100000, 1000, 100);
        roundSpinner.setEditable(true);
        roundSpinner.setValueFactory(valueFactory);
        SpinnerValueFactory<Integer> valueFactoryMax =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000, 1500, 100);
        maxStepsSpinner.setEditable(true);
        maxStepsSpinner.setValueFactory(valueFactoryMax);
        progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());

        EventBusUtils.STATISTICS_LOADED_EVENT.addListener(statisticsWithIds -> Platform.runLater(() -> {
            try {
                init(statisticsWithIds);
                EventBusUtils.LOG_LABEL_EVENT.trigger("Statistics loaded");
            } catch (Exception e) {
                log.error("Could not load statistics window layout", e);
            }
        }));

        instanceStatisticsTableView.managedProperty().bind(instanceStatisticsTableView.visibleProperty());
        instanceStatisticsTableView.visibleProperty().bind(Bindings.createBooleanBinding(() -> !instanceStatisticsTableView.getItems().isEmpty(), instanceStatisticsTableView.itemsProperty()));

        statisticsMeasuresTable.managedProperty().bind(statisticsMeasuresTable.visibleProperty());
        statisticsMeasuresTable.visibleProperty().bind(Bindings.createBooleanBinding(() -> !statisticsMeasuresTable.getItems().isEmpty(), statisticsMeasuresTable.itemsProperty()));
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
                    .mapToDouble(Number::doubleValue)
                    .average().getAsDouble());
        });
        averageColumn.setText("average");
        statisticsMeasuresTable.getColumns().add(averageColumn);

        TableColumn<HashMap.Entry<StatisticObject.StatisticInfo, List<StatisticObject>>, Double> minColumn = new TableColumn<>();
        minColumn.setCellValueFactory(param -> {
            List<StatisticObject> value = param.getValue().getValue();
            return new SimpleObjectProperty<>(value.stream().map(StatisticObject::getValue)
                    .mapToDouble(Number::doubleValue)
                    .min().getAsDouble());
        });
        minColumn.setText("min");
        statisticsMeasuresTable.getColumns().add(minColumn);

        TableColumn<HashMap.Entry<StatisticObject.StatisticInfo, List<StatisticObject>>, Double> maxColumn = new TableColumn<>();
        maxColumn.setCellValueFactory(param -> {
            List<StatisticObject> value = param.getValue().getValue();
            return new SimpleObjectProperty<>(value.stream().map(StatisticObject::getValue)
                    .mapToDouble(Number::doubleValue)
                    .max().getAsDouble());
        });
        maxColumn.setText("max");
        statisticsMeasuresTable.getColumns().add(maxColumn);
    }

    private void instanceTableInit() {
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

    private void addColumns() {
        TableColumn<StatisticsWithId, Integer> idColumn = new TableColumn<>();
        idColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getId()));
        instanceStatisticsTableView.getColumns().add(idColumn);
        idColumn.setText("id");

        statisticsMeasureHashMap.keySet()
                .forEach(statisticInfo -> {
                    TableColumn<StatisticsWithId, Double> statisticColumn = new TableColumn<>();
                    statisticColumn.setCellValueFactory(param -> {
                        StatisticsWithId value = param.getValue();
                        List<StatisticObject> statisticObjects = value.getStatisticObjects();
                        Optional<StatisticObject> first = statisticObjects.stream()
                                .filter(statisticObject -> statisticObject.getStatisticInfo().equals(statisticInfo))
                                .findFirst();
                        StatisticObject statisticObject = first.orElseThrow();
                        return new SimpleObjectProperty<>(statisticObject.getValue().doubleValue());
                    });

                    instanceStatisticsTableView.getColumns().add(statisticColumn);
                    statisticColumn.setText(statisticInfo.getName());
                });
    }

    public void init(StatisticsWithIdsAndPath statisticsWithIdsAndPath) {
        clearTables();
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
        addColumns();
    }

    private void clearTables() {
        statisticsMeasuresTable.getItems().clear();
        instanceStatisticsTableView.getItems().clear();
        instanceStatisticsTableView.getColumns().clear();
    }

    public void init(ObjectProperty<GameManager> gameManager, ComboBox<Class<? extends Searcher>> searcherList, ComboBox<Class<? extends Hider>> hiderList, ComboBox<Class<? extends GameEngine>> gameEngineList) {
        this.gameManager = gameManager;
        this.searcherList = searcherList;
        this.hiderList = hiderList;
        this.gameEngineList = gameEngineList;
        runMultipleButton.disableProperty().bind(searcherList.getSelectionModel().selectedItemProperty().isNull()
                .or(hiderList.getSelectionModel().selectedItemProperty().isNull())
                .or(gameEngineList.getSelectionModel().selectedItemProperty().isNull())
        );

        runMultipleButton.textProperty().bind(
                Bindings.when(
                        searcherList.getSelectionModel().selectedItemProperty().isNull()
                                .or(hiderList.getSelectionModel().selectedItemProperty().isNull())
                                .or(gameEngineList.getSelectionModel().selectedItemProperty().isNull())
                )
                        .then("Select a game")
                        .otherwise("Run multiple games")
        );

        roundSpinner.disableProperty().bind(searcherList.getSelectionModel().selectedItemProperty().isNull()
                .or(hiderList.getSelectionModel().selectedItemProperty().isNull())
                .or(gameEngineList.getSelectionModel().selectedItemProperty().isNull())
        );

    }

    public void onSeriesRun(ActionEvent actionEvent) {
        progressIndicator.setVisible(true);
        try {
            Class<? extends GameEngine> selectedGameEngine = gameEngineList.getSelectionModel().getSelectedItem();
            Class<? extends Searcher> selectedSearcher = searcherList.getSelectionModel().getSelectedItem();
            Class<? extends Hider> selectedHider = hiderList.getSelectionModel().getSelectedItem();
            GameManager newGameManager = new GameManager(selectedSearcher, selectedHider, selectedGameEngine);
            CompletableFuture.runAsync(() ->
                    SeriesService.getInstance().runSeries(roundSpinner.getValue(), newGameManager, aDouble ->
                            Platform.runLater(() ->
                                    progressIndicator.setProgress(aDouble)
                            ), maxStepsSpinner.getValue()==0? null : maxStepsSpinner.getValue()
                    )
            )
                    .exceptionally(throwable -> {
                        log.error("Game Series run failed", throwable);
                        return null;
                    })
                    .thenRun(() -> Platform.runLater(() -> progressIndicator.setVisible(false)
                    ));
        } catch (
                Exception e) {
            EventBusUtils.LOG_LABEL_EVENT.trigger("Could not create new GameManager.");
            log.error("Could not create new GameManager.", e);
        }

    }

    public void onSeriesLoad() {
        SeriesService.getInstance().readStatistics();
    }


    public void copyClipboard() {
        StringSelection stringSelection = new StringSelection(generateCopyString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public String generateCopyString() {
        StringBuilder stringBuilder = new StringBuilder();

        instanceStatisticsTableView.getColumns()
                .forEach(statisticsWithIdTableColumn -> stringBuilder.append(statisticsWithIdTableColumn.getText()).append("\t"));

        stringBuilder.append("\n");

        instanceStatisticsTableView.getItems().forEach(
                statisticsWithId -> {
                    stringBuilder.append(statisticsWithId.getId()).append("\t");
                    statisticsWithId.getStatisticObjects().forEach(
                            statisticObject -> stringBuilder.append(statisticObject.getValue()).append("\t")
                    );
                    stringBuilder.append("\n");
                }
        );
        return stringBuilder.toString();
    }

    @SneakyThrows
    public void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("table.csv");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("A CSV file (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        File dest = fileChooser.showSaveDialog(instanceStatisticsTableView.getScene().getWindow());

        if (dest == null) {
            return;
        }

        CSVWriter writer = new CSVWriter(new FileWriter(dest));
        List<List<String>> table = new ArrayList<>();
        table.add(new ArrayList<>(Arrays.asList(" ")));
        table.get(0).addAll(instanceStatisticsTableView.getItems()
                .stream()
                .map(statisticsWithId -> Integer.toString(statisticsWithId.getId()))
                .collect(Collectors.toList()));

        for (StatisticObject.StatisticInfo statisticInfo : statisticsMeasureHashMap.keySet()) {
            List<String> row = new ArrayList<>();
            row.add(statisticInfo.getName());
            row.addAll(statisticsMeasureHashMap.get(statisticInfo)
                    .stream()
                    .map(statisticObject -> statisticObject.getValue().toString())
                    .collect(Collectors.toList()));
            table.add(row);
        }
        table = ListUtils.transpose(table);
        writer.writeAll(table
                .stream()
                .map(strings -> strings.toArray(String[]::new))
                .collect(Collectors.toList()));
        writer.close();
    }

    public void onPlot() throws IOException {

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Statistic Plot");

        Class<? extends GameEngine> selectedGameEngine = gameEngineList.getSelectionModel().getSelectedItem();
        Class<? extends Searcher> selectedSearcher = searcherList.getSelectionModel().getSelectedItem();
        Class<? extends Hider> selectedHider = hiderList.getSelectionModel().getSelectedItem();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/plotSettings.fxml"));
        GridPane root = fxmlLoader.load();
        PlotSettingsController plotSettingsController = fxmlLoader.getController();
        plotSettingsController.setData(selectedGameEngine, selectedSearcher, selectedHider);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/plot.fxml"));
        Parent plot = loader.load();
        PlotController plotController = loader.getController();
        plotSettingsController.init(settings -> {
            plotSettingsController.errorLabel.getScene().setRoot(plot);
            plotController.setData(settings, selectedGameEngine, selectedSearcher, selectedHider);
            stage.setMaximized(true);
        });

        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/layout/style.css").toExternalForm());

        stage.show();

    }
}