package com.treasure.hunt.view;

import javafx.beans.binding.BooleanBinding;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * The toolbar controller holds two sections for buttons.
 * These buttons show and hide widgets in the corresponding section in the {@link WidgetBarController}.
 *
 * @author jotoh
 */
@Slf4j
public class ToolbarController {
    public Pane first;
    public Pane second;
    @Getter
    private ToggleGroup firstGroup = new ToggleGroup();
    @Getter
    private ToggleGroup secondGroup = new ToggleGroup();

    public void addButton(boolean first, String text, boolean selected, Region widget) {

        if (widget == null) {
            return;
        }

        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setToggleGroup((first ? firstGroup : secondGroup));
        toggleButton.setSelected(selected);

        Group group = new Group();
        group.getChildren().addAll(new Label(text));
        toggleButton.setGraphic(group);

        if (first) {
            this.first.getChildren().add(toggleButton);
        } else {
            this.second.getChildren().add(toggleButton);
        }

        widget.visibleProperty().bind(toggleButton.selectedProperty());

    }

    public BooleanBinding visibleBinding() {
        return firstGroup.selectedToggleProperty().isNotNull().or(secondGroup.selectedToggleProperty().isNotNull());
    }

}
