package com.treasure.hunt.view.settings;

import com.treasure.hunt.service.settings.PrefName;
import com.treasure.hunt.service.settings.Settings;
import com.treasure.hunt.service.settings.SettingsService;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
public class SettingsController {
    public VBox settingsList;
    public Button cancelButton;
    public Button saveButton;

    private ObservableMap<Field, Property<?>> changedValues = FXCollections.observableHashMap();

    private HashMap<String, Pair<BiConsumer<VBox, Field>, Consumer<Field>>> fieldConverter = new HashMap<>();

    private Settings settings = new Settings();

    @Getter
    private BooleanBinding somethingChanged;

    private static void addDescription(VBox vBox, Field field) {

        String description = "";

        PrefName declaredAnnotation = field.getDeclaredAnnotation(PrefName.class);

        if (declaredAnnotation != null) {
            description = declaredAnnotation.prefDesc();
        }

        if (!description.isEmpty()) {
            Label descriptionLabel = new Label(description);
            descriptionLabel.getStyleClass().add("description");
            vBox.getChildren().add(descriptionLabel);
        }
    }

    public void initialize() {
        settings = SettingsService.getInstance().getSettings();

        fieldConverter.put("boolean", new Pair<>(
                        this::addBooleanSetting,
                        field -> ((BooleanProperty) changedValues.get(field)).setValue((boolean) getSetting(field))
                )
        );

        fieldConverter.put("String", new Pair<>(
                this::addStringSetting,
                field -> ((StringProperty) changedValues.get(field)).setValue((String) getSetting(field))
        ));

        fieldConverter.put("int", new Pair<>(
                this::addIntSetting,
                field -> ((IntegerProperty) changedValues.get(field)).setValue((int) getSetting(field))
        ));
        createSettings();
        bindChangedStates();
    }

    private void createSettings() {
        forEachSetting(this::createSetting);
    }

    private void bindChangedStates() {
        changedValues.addListener((InvalidationListener) change -> createChangedBinding());
        createChangedBinding();
        cancelButton.disableProperty().bind(somethingChanged.not());
        saveButton.disableProperty().bind(somethingChanged.not());
    }

    private void createChangedBinding() {
        final Field[] declaredFields = Settings.class.getDeclaredFields();
        Property<?>[] propertyArray = Arrays.stream(declaredFields).map(changedValues::get).filter(Objects::nonNull).toArray(Property<?>[]::new);
        somethingChanged = Bindings.createBooleanBinding(() -> {
            for (Field field : changedValues.keySet()) {
                field.setAccessible(true);
                if (!changedValues.get(field).getValue().equals(getSetting(field))) {
                    return true;
                }
            }
            return false;
        }, propertyArray);
    }

    private void createSetting(Field field) {
        field.setAccessible(true);

        if (field.getDeclaredAnnotation(PrefName.class) == null) {
            return;
        }

        VBox vBox = new VBox();
        vBox.getStyleClass().add("setting-section");
        settingsList.getChildren().add(vBox);

        createSettingInput(vBox, field);

        addDescription(vBox, field);
    }

    private void createSettingInput(VBox vBox, Field field) {
        for (String typeName : fieldConverter.keySet()) {
            if (field.getGenericType().getTypeName().endsWith(typeName)) {
                fieldConverter.get(typeName).getKey().accept(vBox, field);
                break;
            }
        }
    }

    private void addBooleanSetting(VBox vBox, Field field) {
        String name = field.getDeclaredAnnotation(PrefName.class).prefName();
        final boolean value = (boolean) getSetting(field);
        CheckBox checkBox = new CheckBox(name);
        checkBox.setSelected(value);
        vBox.getChildren().add(checkBox);
        changedValues.put(field, checkBox.selectedProperty());
    }

    private void addStringSetting(VBox vBox, Field field) {
        String name = field.getDeclaredAnnotation(PrefName.class).prefName();
        String value = (String) getSetting(field);
        TextField textField = new TextField(value);
        HBox hBox = new HBox(new Label(name), textField);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_LEFT);
        vBox.getChildren().add(hBox);
        changedValues.put(field, textField.textProperty());
    }

    private void addIntSetting(VBox vBox, Field field) {
        String name = field.getDeclaredAnnotation(PrefName.class).prefName();
        int value = (int) getSetting(field);
        TextField textField = new TextField();
        HBox hBox = new HBox(new Label(name), textField);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_LEFT);
        vBox.getChildren().add(hBox);
        SimpleIntegerProperty integerProperty = new SimpleIntegerProperty(value);
        textField.textProperty().bindBidirectional(integerProperty, new StringConverter<>() {

            @Override
            public String toString(Number object) {
                return object == null ? Integer.toString(value) : object.toString();
            }

            @Override
            public Number fromString(String string) {
                if (string == null) {
                    return value;
                } else {
                    try {
                        return Integer.parseInt(string);
                    } catch (NumberFormatException ex) {
                        return value;
                    }
                }
            }

        });
        changedValues.put(field, integerProperty);
    }

    public void cancel() {
        forEachSetting(this::resetProperty);
    }

    private void resetProperty(Field field) {
        for (String typeName : fieldConverter.keySet()) {
            if (field.getGenericType().getTypeName().equals(typeName)) {
                fieldConverter.get(typeName).getValue().accept(field);
                break;
            }
        }
    }

    public void save() {
        forEachSetting(field -> setSetting(field, changedValues.get(field).getValue()));
        somethingChanged.invalidate();
        CompletableFuture.runAsync(() -> SettingsService.getInstance().saveSettings());
    }

    private Object getSetting(Field field) {
        try {
            return field.get(settings);
        } catch (IllegalAccessException e) {
            log.error("Could not access field '" + field.getName() + "' for getter operation", e);
        }
        return null;
    }

    private void setSetting(Field field, Object object) {
        try {
            field.set(settings, object);
        } catch (IllegalAccessException e) {
            log.error("Could not access field '" + field.getName() + "' for setter operation", e);
        }
    }

    private void forEachSetting(Consumer<Field> consumer) {
        for (Field field : Settings.class.getDeclaredFields()) {
            field.setAccessible(true);
            consumer.accept(field);
        }
    }
}
