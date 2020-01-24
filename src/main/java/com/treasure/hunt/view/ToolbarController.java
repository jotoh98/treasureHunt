package com.treasure.hunt.view;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.Getter;

/**
 * @author jotoh
 */
public class ToolbarController {
    @FXML
    private VBox toolbar;
    @Getter
    private ToggleGroup toggleGroup = new ToggleGroup();

    public void initialize() {
    }

    public void addButton(String text, boolean selected, Pane widget) {
        ToggleButton toggleButton = new ToggleButton();
        toolbar.getChildren().add(toggleButton);
        toggleButton.setToggleGroup(toggleGroup);
        toggleButton.setSelected(selected);
        Group group = new Group();
        toggleButton.setGraphic(group);
        group.getChildren().addAll(new Label(text));

        if (widget != null) {
            widget.visibleProperty().bind(toggleButton.selectedProperty());
        }
    }
}
