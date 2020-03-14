package com.treasure.hunt.view;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.utils.EventBusUtils;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SelectClickedPopUp {
    @Getter
    private ListView<GeometryItem<?>> listView = new ListView<>();
    private CompletableFuture<GeometryItem<?>> geometryItemCompletableFuture = new CompletableFuture<>();

    public SelectClickedPopUp() {
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(GeometryItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getGeometryType() == null) {
                    setText(null);
                } else {
                    setText(item.getGeometryType().getDisplayName());
                }
            }
        });

        listView.setOnMouseClicked(event -> {
            if (geometryItemCompletableFuture == null) {
                return;
            }
            GeometryItem<?> selectedItem = listView.getSelectionModel().getSelectedItem();
            geometryItemCompletableFuture.complete(selectedItem);
            EventBusUtils.INNER_POP_UP_EVENT_CLOSE.trigger(null);
        });

        listView.setOnKeyPressed(event -> {
            if (geometryItemCompletableFuture == null) {
                return;
            }
            if (event.getCode().equals(KeyCode.ENTER)) {
                GeometryItem<?> focusedItem = listView.getFocusModel().getFocusedItem();
                geometryItemCompletableFuture.complete(focusedItem);
                EventBusUtils.INNER_POP_UP_EVENT_CLOSE.trigger(null);
            }
        });
    }

    public CompletableFuture<GeometryItem<?>> getCorrectItem(List<GeometryItem<?>> itemsToBeChosenFrom) {
        listView.setItems(FXCollections.observableArrayList(itemsToBeChosenFrom));
        listView.setPrefHeight(listView.getItems().size() * 24);
        listView.requestFocus();
        listView.getFocusModel().focus(0);
        return geometryItemCompletableFuture;
    }

    public void close() {
        EventBusUtils.INNER_POP_UP_EVENT_CLOSE.trigger(null);
        geometryItemCompletableFuture.complete(null);
    }
}
