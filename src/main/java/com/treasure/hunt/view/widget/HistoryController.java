package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.game.Turn;
import com.treasure.hunt.view.utils.TreeConstructor;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HistoryController {
    public ListView<Turn> list;
    public TitledPane collapse;
    public Pane wrapper;
    public SplitPane split;
    public Pane collapseWrap;

    private double splitPosition = 0.5;

    public void initialize() {
        split.prefWidthProperty().bind(wrapper.widthProperty());
        split.prefHeightProperty().bind(wrapper.heightProperty());
        list.prefWidthProperty().bind(wrapper.widthProperty());
        collapse.prefWidthProperty().bind(wrapper.widthProperty());
    }

    public void init(ObjectProperty<GameManager> gameManager) {
        bindToGameManager(gameManager.get());
        gameManager.addListener((gameManagerProp, oldManager, newManager) -> bindToGameManager(newManager));
    }

    private void bindToGameManager(GameManager gameManager) {
        if (gameManager == null) {
            return;
        }

        final ObservableList<Turn> turns = gameManager.getTurns();

        collapse.collapsibleProperty()
                .bind(Bindings.createBooleanBinding(() -> turns.size() > 0, turns));

        list.setItems(turns);
        list.getSelectionModel().select(0);
        list.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Turn turn, boolean empty) {
                super.updateItem(turn, empty);

                if (turn == null || empty) {
                    setText(null);
                    return;
                }
                int index = turns.indexOf(turn);
                if (index == 0) {
                    setText("Initialization");
                } else {
                    setText(String.format("Turn #%s", index));
                }
            }
        });

        list.getSelectionModel().selectedIndexProperty().addListener((observable, oldIndex, newIndex) -> {
            int index = newIndex.intValue();
            if (index >= 0 && index < turns.size()) {
                gameManager.getViewIndex().setValue(index);
                TreeView<String> tree = TreeConstructor.getTree(turns.get(index));
                tree.prefWidthProperty().bind(collapse.widthProperty());
                collapse.setContent(tree);
                if (index == 0) {
                    collapse.setText("Initialization");
                } else {
                    collapse.setText(String.format("Turn #%s", index));
                }
            }
        });

        collapse.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                split.setDividerPosition(0, splitPosition);
            } else {
                splitPosition = split.getDividerPositions()[0];
                split.setDividerPosition(0, 1.0 - 30 / split.getHeight());
            }

        });

        gameManager.getViewIndex().addListener((observable, oldValue, newValue) -> {
            list.getSelectionModel().select(newValue.intValue());
        });
    }
}
