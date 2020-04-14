package com.treasure.hunt.view.plot;

import com.treasure.hunt.analysis.Statistic;
import com.treasure.hunt.analysis.StatisticsWithIdsAndPath;
import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.service.io.SeriesService;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JavaFxUtils;
import com.treasure.hunt.view.custom.CoinProgress;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
public class PlotController {
    public LineChart<Number, Number> lineChart;
    public NumberAxis xAxis;
    public NumberAxis yAxis;
    public CoinProgress coinProgress;

    public void initialize() {
        coinProgress.managedProperty().bind(coinProgress.visibleProperty());
    }

    public List<Pair<Double, Double>> computeMultipleSeries(PlotSettingsController.Settings settings, Class<? extends GameEngine> selectedGameEngine, Class<? extends Searcher> selectedSearcher, Class<? extends Hider> selectedHider) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        List<Pair<Double, Double>> computedValues = new ArrayList<>();
        int step = 0;
        for (double currentStatisticValue = settings.getLowerBoundValue(); currentStatisticValue < settings.getUpperBoundValue(); currentStatisticValue += settings.getStepSizeValue(), step += 1) {

            log.info("Currently at {}", step);
            PreferenceService.getInstance().putPreference(settings.getPreferenceName(), currentStatisticValue);
            GameManager newGameManager = new GameManager(selectedSearcher, selectedHider, selectedGameEngine);

            StatisticsWithIdsAndPath currentStatistics = SeriesService.getInstance().runSeriesAndSaveToFile(settings.getSeriesAccuracyValue(), newGameManager, a -> {
            }, null, false, true, settings.getMaxSteps());

            List<Number> statisticValues = Statistic.filterBy(currentStatistics.getStatisticsWithIds(), settings.getStatisticInfo());
            computedValues.add(new Pair<>(currentStatisticValue, settings.getType().getAggregation().apply(statisticValues).doubleValue()));
            int finalStep = step;
            Platform.runLater(() -> coinProgress.setProgress(finalStep / (settings.getUpperBoundValue() - settings.getLowerBoundValue())));
        }
        Platform.runLater(() -> coinProgress.setVisible(false));

        return computedValues;
    }

    public void setData(PlotSettingsController.Settings settings, Class<? extends GameEngine> selectedGameEngine, Class<? extends Searcher> selectedSearcher, Class<? extends Hider> selectedHider) {
        lineChart.setTitle(selectedSearcher.getSimpleName());
        xAxis.setLabel(settings.getPreferenceName());
        yAxis.setLabel(settings.getType().name() + "_OF_" + settings.getStatisticInfo().getName());
        CompletableFuture.supplyAsync(() -> {
            try {
                return computeMultipleSeries(settings, selectedGameEngine, selectedSearcher, selectedHider);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(doubles -> {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            doubles
                    .forEach(doublePair -> series.getData().add(new XYChart.Data<>(doublePair.getValue0(), doublePair.getValue1())));
            Platform.runLater(() -> {
                lineChart.getData().add(series);
                if (settings.isSavePNG()) {
                    Platform.runLater(() -> JavaFxUtils.savePngFromStage(lineChart.getScene().getWindow()));
                }
            });
        }).exceptionally(throwable -> {
            log.error("Error calculating plot", throwable);
            return null;
        });
    }
}
