package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.game.Turn;
import com.treasure.hunt.view.utils.TreeConstructor;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HistoryController {
    public ListView<Turn> list;
    public TitledPane collapse;
    public SplitPane split;
    private double splitPosition = 0.5;

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

        list.getSelectionModel().select(0);

        collapse.expandedProperty().addListener((observable, wasExpanded, isExpanded) -> {
            if (isExpanded) {
                split.setDividerPosition(0, splitPosition);
            } else {
                splitPosition = split.getDividerPositions()[0];
                split.setDividerPosition(0, 1.0 - 30 / split.getHeight());
            }
            split.lookupAll(".split-pane-divider").forEach(divider -> divider.setMouseTransparent(!isExpanded));
        });

        gameManager.getViewIndex().addListener((observable, oldValue, newValue) -> {
            list.getSelectionModel().select(newValue.intValue());
            list.scrollTo(newValue.intValue());
        });
    }
}
