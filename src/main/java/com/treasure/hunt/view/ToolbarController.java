package com.treasure.hunt.view;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import lombok.Getter;

/**
 * Controller for the toolbar.
 * The toolbar holds the toggle buttons for the individual widgets placed inside of their {@link WidgetBarController}.
 *
 * @author jotoh
 * @see WidgetBarController
 */
public class ToolbarController {
    /**
     * The toolbar pane where the buttons lay.
     */
    @FXML
    private Pane toolbar;

    /**
     * The toggle group of the toolbar. Only one widget is active at a time.
     */
    @Getter
    private ToggleGroup toggleGroup = new ToggleGroup();

    /**
     * Adds a button for a widget.
     * Creates all the necessary constraints.
     *
     * @param text     button text
     * @param selected whether the button is selected or not
     * @param widget   widget associated with this button
     */
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
