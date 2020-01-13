package com.treasure.hunt;

import com.treasure.hunt.analysis.StatisticsWithIdsAndPath;
import com.treasure.hunt.utils.EventBusUtils;
import com.treasure.hunt.view.StatisticsWindowController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jotoh
 */
@Slf4j
public class JavaFxApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        scene.getStylesheets().add(getClass().getResource("/layout/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> Platform.exit());
        listenToStatics();
    }

    private void listenToStatics() {
        EventBusUtils.STATISTICS_LOADED_EVENT.addListener(statisticsWithIds -> Platform.runLater(() -> {
            try {
                loadStatisticsWindow(statisticsWithIds);
            } catch (Exception e) {
                log.error("Could not load statistics window layout", e);
            }
        }));
    }

    private void loadStatisticsWindow(StatisticsWithIdsAndPath statisticsWithIdsAndPath) throws Exception {
        final Stage stage = new Stage();
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/statistics.fxml"));

        Scene scene = new Scene(fxmlLoader.load());

        StatisticsWindowController controller = fxmlLoader.getController();
        controller.init(statisticsWithIdsAndPath);

        scene.getStylesheets().add(getClass().getResource("/layout/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
