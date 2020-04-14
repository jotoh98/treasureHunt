package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.geom.StatusMessageItem;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author dorianreineccius
 */
@Slf4j
public class StatusMessageWidgetController {
    public VBox vbox;
    private final ChangeListener<List<StatusMessageItem>> statusChangeListener = (observable1, oldValue1, newStatus) -> renderNewStatus(newStatus);
    private ObjectBinding<List<StatusMessageItem>> oldStatusBinding;

    public void initialize() {

    }

    public void init(ObjectProperty<GameManager> gameManager) {
        ChangeListener<GameManager> gameManagerChangeListener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (oldStatusBinding != null) {
                    oldStatusBinding.removeListener(statusChangeListener);
                }
                oldStatusBinding = gameManager.get()
                        .getStatusMessageItemsBinding();
                oldStatusBinding
                        .addListener(statusChangeListener);
                renderNewStatus(gameManager.get().getStatusMessageItemsBinding().get());
            }
        };
        gameManager.addListener(gameManagerChangeListener);
        gameManagerChangeListener.changed(null, null, gameManager.get());
    }

    private void renderNewStatus(List<StatusMessageItem> statusMessageItems) {
        Platform.runLater(() -> {
            vbox.getChildren().clear();
            statusMessageItems.forEach(this::addItem);
        });
    }

    @SneakyThrows
    private void addItem(StatusMessageItem statusMessageItem) {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/statusMessageWidgetTextFlow.fxml"));

        TextFlow load = fxmlLoader.load();
        vbox.getChildren().add(load);

        StatusMessageWidgetTextFlowController controller = fxmlLoader.getController();
        controller.setData(statusMessageItem.getStatusMessageType().getName(), statusMessageItem.getMessage());
    }
}

