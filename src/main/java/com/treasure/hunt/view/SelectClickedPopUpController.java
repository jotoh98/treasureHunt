package com.treasure.hunt.view;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.utils.EventBusUtils;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SelectClickedPopUpController {
    public ListView<GeometryItem<?>> selectListView;
    private CompletableFuture<GeometryItem<?>> geometryItemCompletableFuture = new CompletableFuture<>();

    public void initialize() {
        selectListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(GeometryItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getGeometryType() == null) {
                    setText(null);
                } else {
                    setText(item.getGeometryType().getDisplayName());
                    setOnMouseClicked(mouseEvent -> {
                        if (geometryItemCompletableFuture != null) {
                            geometryItemCompletableFuture.complete(item);
                            EventBusUtils.INNER_POP_UP_EVENT_CLOSE.trigger(null);
                        }
                    });
                }

            }
        });
    }

    public CompletableFuture<GeometryItem<?>> getCorrectItem(List<GeometryItem<?>> itemsToBeChosenFrom) {
        selectListView.setItems(FXCollections.observableArrayList(itemsToBeChosenFrom));
        return geometryItemCompletableFuture;
    }

    public void close() {
        EventBusUtils.INNER_POP_UP_EVENT_CLOSE.trigger(null);
        geometryItemCompletableFuture.complete(null);
    }
}
