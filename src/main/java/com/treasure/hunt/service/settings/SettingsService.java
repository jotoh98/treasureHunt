package com.treasure.hunt.service.settings;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;
import java.text.MessageFormat;

@Slf4j
public class SettingsService {

    private static final String SESSION_PATH = MessageFormat.format("{0}{1}.treasureHunt{1}session.kryo", System.getProperty("user.home"), File.separator);
    private static final String SETTINGS_PATH = MessageFormat.format("{0}{1}.treasureHunt{1}settings.kryo", System.getProperty("user.home"), File.separator);

    private static SettingsService instance = null;
    private Kryo kryo = new Kryo();

    @Getter
    @Setter
    private Session session = new Session();

    @Getter
    @Setter
    private Settings settings = new Settings();

    private SettingsService() {
        kryo.register(Session.class);
        kryo.register(Settings.class);
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
    }

    public synchronized static SettingsService getInstance() {
        if (instance == null) {
            instance = new SettingsService();
        }
        return instance;
    }

    public void loadSession() {
        try (FileInputStream fileInputStream = new FileInputStream(SESSION_PATH)) {
            session = kryo.readObject(new Input(fileInputStream), Session.class);
        } catch (FileNotFoundException e) {
            log.info("Save file could not be found", e);
        } catch (IOException e) {
            log.error("Error", e);
        } catch (KryoException e) {
            session = new Session();
            saveSession();
        }
    }

    public void loadSettings() {
        try (FileInputStream fileInputStream = new FileInputStream(SETTINGS_PATH)) {
            settings = kryo.readObject(new Input(fileInputStream), Settings.class);
        } catch (FileNotFoundException e) {
            log.info("Save file could not be found", e);
        } catch (IOException e) {
            log.error("Error", e);
        } catch (KryoException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Could not read the save file");
            alert.setContentText("Save file not readable. Will be overridden by default settings");
            alert.showAndWait();
            settings = new Settings();
            saveSettings();
        }
    }

    public void saveSession() {
        if (!create(SESSION_PATH)) {
            return;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(SESSION_PATH)) {
            final Output output = new Output(fileOutputStream);
            kryo.writeObject(output, session);
            output.flush();
        } catch (FileNotFoundException e) {
            log.error("Save file not found", e);
            e.printStackTrace();
        } catch (IOException e) {
            log.error("Error saving the settings", e);
        }
    }

    public void saveSettings() {
        if (!create(SETTINGS_PATH)) {
            return;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(SETTINGS_PATH)) {
            final Output output = new Output(fileOutputStream);
            kryo.writeObject(output, settings);
            output.flush();
        } catch (FileNotFoundException e) {
            log.error("Save file not found", e);
            e.printStackTrace();
        } catch (IOException e) {
            log.error("Error saving the settings", e);
        }
    }

    private boolean create(File file) {

        if (file.exists()) {
            return true;
        }

        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            return false;
        }

        try {
            if (!file.createNewFile()) {
                log.info("Save file could not be created");
                return false;
            }
        } catch (IOException e) {
            log.error("Error creating the save file", e);
            return false;
        }

        return true;
    }

    private boolean create(String path) {
        return create(new File(path));
    }

    public void setup() {
        File sessionFile = new File(SESSION_PATH);
        File settingsFile = new File(SETTINGS_PATH);

        if (!sessionFile.exists()) {
            create(sessionFile);
            session = new Session();
            saveSession();
        } else {
            loadSession();
        }

        if (!settingsFile.exists()) {
            create(settingsFile);
            settings = new Settings();
            saveSettings();
        } else {
            loadSettings();
        }
    }
}
