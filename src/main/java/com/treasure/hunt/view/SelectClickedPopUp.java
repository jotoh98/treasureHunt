package com.treasure.hunt.view;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.utils.EventBusUtils;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SelectClickedPopUp {
    @Getter
    private ListView<GeometryItem<?>> listView = new ListView<>();
    private VBox vBox = new VBox();
    private CompletableFuture<GeometryItem<?>> geometryItemCompletableFuture = new CompletableFuture<>();

    public SelectClickedPopUp() {

        vBox.getChildren().addAll(listView);
        VBox.setVgrow(listView, Priority.ALWAYS);

        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setCellFactory(param -> {
            ListCell<GeometryItem<?>> listCell = new ListCell<>() {
                @Override
                protected void updateItem(GeometryItem item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null || item.getGeometryType() == null) {
                        setText(null);
                    } else {
                        setText(item.getGeometryType().getDisplayName());
                        setPrefHeight(25);
                        if (isHover()) {
                            requestFocus();
                        }
                    }

                }


            };
            listCell.hoverProperty().addListener((observable, wasHovered, isHovered) -> {
                if (isHovered && !listCell.isEmpty()) {
                    listCell.requestFocus();
                }
            });
            return listCell;
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

    public Node getPopUp() {
        return vBox;
    }

    public CompletableFuture<GeometryItem<?>> getCorrectItem(List<GeometryItem<?>> itemsToBeChosenFrom) {
        listView.setItems(FXCollections.observableArrayList(itemsToBeChosenFrom));
        listView.maxHeightProperty().bind(Bindings.min(5, Bindings.size(listView.getItems())).multiply(25));
        return geometryItemCompletableFuture;
    }
}
