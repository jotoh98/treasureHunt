package com.treasure.hunt.service.preferences;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Service that holds all the preferences, you can pass those to the searcher and hider. Either preferences are set via the UI or via env variables that start with PREF_ followed by the preference name.
 * Preference identifiers should be written in upper snail case and can only be numbers.
 */
@Slf4j
public class PreferenceService {
    public static final String PREF_PREFIX = "PREF_";
    private static PreferenceService instance = new PreferenceService();
    @Getter
    private ObservableMap<String, Number> preferences = FXCollections.observableHashMap();

    private PreferenceService() {
        System.getenv().
                forEach((key, value) -> {
                    if (key.startsWith(PREF_PREFIX)) {
                        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
                        try {
                            preferences.put(key.substring(PREF_PREFIX.length()), numberFormat.parse(value));
                        } catch (ParseException e) {
                            log.warn("Found an env variable that starts with the preference prefix but is no number name: {} , value: {}", key, value);
                        }
                    }
                });
    }

    public static PreferenceService getInstance() {
        return instance;
    }

    public void putPreference(String name, Number value) {
        preferences.put(name, value);
    }

    public void deletePreferences(String name) {
        preferences.remove(name);
    }

    public Number getPreference(String name, Number defaultNumber) {
        return preferences.getOrDefault(name, defaultNumber);
    }

}
