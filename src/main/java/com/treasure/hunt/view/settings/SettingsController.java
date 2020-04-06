package com.treasure.hunt.view.settings;

import com.treasure.hunt.service.settings.Settings;
import com.treasure.hunt.service.settings.SettingsService;
import com.treasure.hunt.service.settings.UserSetting;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
public class SettingsController {
    /**
     * List of setting boxes.
     */
    public VBox settingsList;

    /**
     * Button to cancel the settings changes.
     */
    public Button cancelButton;

    /**
     * Button to save the altered settings.
     */
    public Button saveButton;

    /**
     * Observable list of changed settings fields.
     */
    private ObservableMap<Field, ReadOnlyProperty<?>> changedValues = FXCollections.observableHashMap();

    /**
     * Maps a {@link java.lang.reflect.Type} by name to
     * - a method to add a setting
     * - a method to retrieve the value from the settings box.
     */
    private HashMap<String, Pair<BiConsumer<VBox, Field>, Consumer<Field>>> fieldConverter = new HashMap<>();

    /**
     * Settings instance to save and read.
     */
    private Settings settings = new Settings();

    /**
     * Binding for when a setting has changed.
     */
    @Getter
    private BooleanBinding somethingChanged;

    /**
     * Add a new description to a setting box.
     *
     * @param vBox  setting box
     * @param field annotated field in {@link Settings}
     * @see UserSetting
     */
    private static void addDescription(VBox vBox, Field field) {

        String description = "";

        UserSetting declaredAnnotation = field.getDeclaredAnnotation(UserSetting.class);

        if (declaredAnnotation != null) {
            description = declaredAnnotation.desc();
        }

        if (!description.isEmpty()) {
            Label descriptionLabel = new Label(description);
            descriptionLabel.setMinHeight(Double.NEGATIVE_INFINITY);
            descriptionLabel.setWrapText(true);
            descriptionLabel.getStyleClass().add("description");
            vBox.getChildren().add(descriptionLabel);
        }
    }

    /**
     * Setup of the settings window.
     * Binds the field converters.
     */
    public void initialize() {
        settings = SettingsService.getInstance().getSettings();

        fieldConverter.put("boolean", new Pair<>(
                this::addBooleanSetting,
                field -> ((BooleanProperty) changedValues.get(field)).setValue((boolean) getSetting(field))
        ));

        fieldConverter.put("String", new Pair<>(
                this::addStringSetting,
                field -> ((StringProperty) changedValues.get(field)).setValue((String) getSetting(field))
        ));

        fieldConverter.put("int", new Pair<>(
                this::addIntSetting,
                field -> ((ObjectProperty<Locale>) changedValues.get(field)).setValue((Locale) getSetting(field))
        ));

        fieldConverter.put("Locale", new Pair<>(
                this::addLocaleSetting,
                field -> ((IntegerProperty) changedValues.get(field)).setValue((int) getSetting(field))
        ));

        createSettings();
        bindChangedStates();
    }

    /**
     * Creates the settings boxes for each field.
     */
    private void createSettings() {
        forEachSetting(this::createSetting);
    }

    /**
     * Bind settings change to buttons being disabled.
     */
    private void bindChangedStates() {
        changedValues.addListener((InvalidationListener) change -> createChangedBinding());
        createChangedBinding();
        saveButton.disableProperty().bind(somethingChanged.not());
    }

    /**
     * Create bindings for the state of changed settings.
     */
    private void createChangedBinding() {
        final Field[] declaredFields = Settings.class.getDeclaredFields();
        ReadOnlyProperty<?>[] propertyArray = Arrays.stream(declaredFields).map(changedValues::get).filter(Objects::nonNull).toArray(ReadOnlyProperty<?>[]::new);
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

    /**
     * Create a setting for a field in the {@link Settings} class.
     *
     * @param field field of settings class
     */
    private void createSetting(Field field) {
        field.setAccessible(true);

        if (field.getDeclaredAnnotation(UserSetting.class) == null) {
            return;
        }

        VBox vBox = new VBox();
        vBox.getStyleClass().add("setting-section");
        settingsList.getChildren().add(vBox);

        createSettingInput(vBox, field);

        addDescription(vBox, field);
    }

    /**
     * Create the input of the fields value and add it to the sections list.
     *
     * @param vBox  list of settings sections
     * @param field field which holds the value of the setting
     */
    private void createSettingInput(VBox vBox, Field field) {
        for (String typeName : fieldConverter.keySet()) {
            if (field.getGenericType().getTypeName().endsWith(typeName)) {
                fieldConverter.get(typeName).getKey().accept(vBox, field);
                break;
            }
        }
    }

    /**
     * Add a boolean setting section. Creates a labeled checkbox.
     *
     * @param vBox  list of settings sections
     * @param field boolean field
     */
    private void addBooleanSetting(VBox vBox, Field field) {
        String name = field.getDeclaredAnnotation(UserSetting.class).name();
        final boolean value = (boolean) getSetting(field);
        CheckBox checkBox = new CheckBox(name);
        checkBox.setSelected(value);
        vBox.getChildren().add(checkBox);
        changedValues.put(field, checkBox.selectedProperty());
    }

    /**
     * Add a string setting section. Creates a labeled text field.
     *
     * @param vBox  list of settings sections
     * @param field string field
     */
    private void addStringSetting(VBox vBox, Field field) {
        String name = field.getDeclaredAnnotation(UserSetting.class).name();
        String value = (String) getSetting(field);
        TextField textField = new TextField(value);
        HBox hBox = new HBox(new Label(name), textField);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_LEFT);
        vBox.getChildren().add(hBox);
        changedValues.put(field, textField.textProperty());
    }

    /**
     * Add a integer setting section. Creates a labeled text field.
     *
     * @param vBox  list of settings sections
     * @param field integer field
     */
    private void addIntSetting(VBox vBox, Field field) {
        String name = field.getDeclaredAnnotation(UserSetting.class).name();
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

    /**
     * Add a locale setting section. Creates a locale drop down.
     *
     * @param vBox  list of settings sections
     * @param field locale field
     */
    private void addLocaleSetting(VBox vBox, Field field) {
        String name = field.getDeclaredAnnotation(UserSetting.class).name();
        Locale value = (Locale) getSetting(field);
        final ComboBox<Locale> localeComboBox = new ComboBox<>();
        localeComboBox.setItems(FXCollections.observableArrayList(Locale.getAvailableLocales()).sorted());
        localeComboBox.getSelectionModel().select(value);
        HBox hBox = new HBox(new Label(name), localeComboBox);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_LEFT);
        vBox.getChildren().add(hBox);
        changedValues.put(field, localeComboBox.getSelectionModel().selectedItemProperty());
    }

    /**
     * Cancel action. Closes the settings window.
     */
    public void cancel() {
        settingsList.getScene().getWindow().hide();
    }

    /**
     * Save action overrides the settings.
     * Thus invalidating the "something-changed" bindings.
     */
    public void save() {
        forEachSetting(field -> setSetting(field, changedValues.get(field).getValue()));
        somethingChanged.invalidate();
        CompletableFuture.runAsync(() -> SettingsService.getInstance().saveSettings());
    }

    /**
     * Get a setting value for a field.
     *
     * @param field field to get value from
     * @return fields value
     */
    private Object getSetting(Field field) {
        try {
            return field.get(settings);
        } catch (IllegalAccessException e) {
            log.error("Could not access field '" + field.getName() + "' for getter operation", e);
        }
        return null;
    }

    /**
     * Set a setting value in a field.
     *
     * @param field  field to set value in
     * @param object value to set
     */
    private void setSetting(Field field, Object object) {
        try {
            field.set(settings, object);
        } catch (IllegalAccessException e) {
            log.error("Could not access field '" + field.getName() + "' for setter operation", e);
        }
    }

    /**
     * Execute an action on each field in settings.
     *
     * @param consumer action to perform upon every field
     */
    private void forEachSetting(Consumer<Field> consumer) {
        for (Field field : Settings.class.getDeclaredFields()) {
            field.setAccessible(true);
            consumer.accept(field);
        }
    }
}
