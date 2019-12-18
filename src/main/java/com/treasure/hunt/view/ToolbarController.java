package com.treasure.hunt.view;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.Getter;

/**
 * @author hassel
 */
public class ToolbarController {

    @FXML
    private VBox toolbar;

    @Getter
    private ToggleGroup toggleGroup = new ToggleGroup();

    public void initialize() {
    }

    public void bindWidgetBar(SplitPane.Divider divider, Pane widgetBar) {
        widgetBar.visibleProperty().bind(toggleGroup.selectedToggleProperty().isNotNull());
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

    public void addButton(String text) {
        addButton(text, false, null);
    }

    public ObservableList<Node> getButtons() {
        return toolbar.getChildren();
    }

    public ToggleButton getButton(int i) {
        return (ToggleButton) toolbar.getChildren().get(i);
    }
}
