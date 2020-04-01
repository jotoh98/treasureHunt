package com.treasure.hunt.view.settings;

import com.treasure.hunt.Main;
import com.treasure.hunt.view.widget.Widget;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class SettingsWindow {

    public static void show() {
        Stage stage = new Stage();
        final Widget<SettingsController, Region> settings = new Widget<>("/layout/settings/settings.fxml");
        Scene scene = new Scene(settings.getComponent());
        scene.getStylesheets().add(Main.class.getResource("/layout/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Settings");
        stage.setMaxHeight(500);
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> exitClicked(event, settings.getController()));
        stage.show();
    }

    public static void exitClicked(WindowEvent event, SettingsController controller) {
        if (controller.getSomethingChanged().get()) {
            Alert savedAlert = new Alert(Alert.AlertType.CONFIRMATION);
            savedAlert.setHeaderText("You didn't save");
            savedAlert.setContentText("If you confirm, all changes to your settings are discarded.");
            Optional<ButtonType> buttonPressed = savedAlert.showAndWait();

            if (buttonPressed.isPresent() && buttonPressed.get() != ButtonType.OK) {
                event.consume();
            }
        }
    }
}
