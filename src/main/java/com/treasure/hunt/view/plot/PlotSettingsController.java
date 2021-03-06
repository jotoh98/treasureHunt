package com.treasure.hunt.view.plot;

import com.treasure.hunt.analysis.StatisticAggregation;
import com.treasure.hunt.analysis.StatisticObject;
import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import lombok.Value;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlotSettingsController {
    public CheckBox savePNGCheckBox;
    public ComboBox<String> selectPreference;
    public ComboBox<StatisticObject.StatisticInfo> selectStatistic;
    public TextField lowerBound;
    public TextField upperBound;
    public TextField stepSize;
    public TextField seriesAccuracy;
    public ComboBox<StatisticAggregation> selectAggregationTypeCombo;
    public Label errorLabel;
    public TextField maxStepField;
    private Class<? extends GameEngine> selectedGameEngine;
    private Class<? extends Searcher> selectedSearcher;
    private Class<? extends Hider> selectedHider;
    private Consumer<Settings> settingsConsumer;

    public void init(Consumer<Settings> consumer) {
        settingsConsumer = consumer;
    }

    public void onSubmit() throws IOException {
        double lowerBoundValue;
        double upperBoundValue;
        double stepSizeValue;
        int seriesAccuracyValue;
        try {
            lowerBoundValue = Double.parseDouble(lowerBound.getText());
        } catch (Exception ignored) {
            error("Please fill out lower bound correctly");
            return;
        }
        try {
            upperBoundValue = Double.parseDouble(upperBound.getText());
        } catch (Exception ignored) {
            error("Please fill out upper bound correctly");
            return;
        }
        try {
            stepSizeValue = Double.parseDouble(stepSize.getText());
        } catch (Exception ignored) {
            error("Please fill out step size correctly");
            return;
        }
        try {
            seriesAccuracyValue = Integer.parseInt(seriesAccuracy.getText());
        } catch (Exception ignored) {
            error("Please fill out series accuracy correctly");
            return;
        }
        if (lowerBoundValue > upperBoundValue) {
            error("Lower bound is bigger than upper bound");
            return;
        }

        if (seriesAccuracyValue < 0) {
            error("Series rounds must be bigger 0");
            return;
        }

        if (stepSizeValue <= 0) {
            error("Step size value should be reasonable");
            return;
        }

        StatisticAggregation aggregationTypeComboValue = selectAggregationTypeCombo.getValue();
        if (aggregationTypeComboValue == null) {
            error("Choose a Type");
            return;
        }

        StatisticObject.StatisticInfo statisticValue = selectStatistic.getValue();
        if (statisticValue == null) {
            error("Choose a Statistic");
            return;
        }

        String preferenceValue = selectPreference.getValue();
        if (preferenceValue == null || preferenceValue.isEmpty()) {
            error("Choose a Preference");
            return;
        }

        Integer maxSteps = null;
        try {
            maxSteps = Integer.parseInt(maxStepField.getText());
        } catch (Exception ignored) {

        }

        Settings settings = new Settings(aggregationTypeComboValue, statisticValue, preferenceValue, lowerBoundValue, upperBoundValue, stepSizeValue, seriesAccuracyValue, maxSteps,  savePNGCheckBox.isSelected());
        settingsConsumer.accept(settings);

    }

    public void onCancel() {
        errorLabel.getScene().getWindow().hide();
    }

    public void initialize() {
        errorLabel.setVisible(false);
    }

    private void error(String errorString) {
        errorLabel.setText(errorString);
        errorLabel.setVisible(true);
    }

    public void setData(Class<? extends GameEngine> selectedGameEngine, Class<? extends Searcher> selectedSearcher, Class<? extends Hider> selectedHider) {
        initPreferenceComboBox();
        initSelectStatisticComboBox();
        initSelectAggregationComboBox();
        this.selectedGameEngine = selectedGameEngine;
        this.selectedSearcher = selectedSearcher;
        this.selectedHider = selectedHider;

    }

    private void initSelectAggregationComboBox() {
        selectAggregationTypeCombo.getItems().setAll(StatisticAggregation.values());
        selectAggregationTypeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(StatisticAggregation statisticAggregation) {
                return statisticAggregation == null ? null : statisticAggregation.name();
            }

            @Override
            public StatisticAggregation fromString(String s) {
                throw new UnsupportedOperationException();
            }
        });
    }

    private void initSelectStatisticComboBox() {
        List<StatisticObject.StatisticInfo> allStatisticInfo = StatisticObject.StatisticInfo.getAllStatisticInfo();
        selectStatistic.getItems().setAll(allStatisticInfo);
        selectStatistic.setConverter(new StringConverter<>() {
            @Override
            public String toString(StatisticObject.StatisticInfo info) {
                return info == null ? null : info.getName();
            }

            @Override
            public StatisticObject.StatisticInfo fromString(String s) {
                throw new UnsupportedOperationException();
            }
        });
    }

    private void initPreferenceComboBox() {
        final List<String> preferenceNames = Arrays.stream(PreferenceService.class.getDeclaredFields())
                .filter(field -> field.getType().equals(String.class))
                .filter(field -> !field.getName().equals("PREF_PREFIX"))
                .peek(field -> field.setAccessible(true))
                .map(field -> {
                    try {
                        return (String) field.get(null);
                    } catch (Exception ignored) {
                    }
                    return "";
                })
                .filter(string -> !string.isEmpty())
                .collect(Collectors.toList());

        selectPreference.getItems().setAll(preferenceNames);
    }

    @Value
    public static class Settings {
        StatisticAggregation type;
        StatisticObject.StatisticInfo statisticInfo;
        String preferenceName;
        double lowerBoundValue;
        double upperBoundValue;
        double stepSizeValue;
        int seriesAccuracyValue;
        Integer maxSteps;
        boolean savePNG;
    }


}
