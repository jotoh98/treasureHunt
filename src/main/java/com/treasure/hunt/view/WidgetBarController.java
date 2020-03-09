package com.treasure.hunt.view;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * Controller for a widget bar.
 * Simply a wrapper for the widget placed in it.
 * The associated button lays in the {@link WidgetBarController}.
 *
 * @author jotoh
 * @see WidgetBarController
 */
public class WidgetBarController {
    /**
     * Widget wrapper pane.
     */
    @FXML
    private Pane widgetBar;

    /**
     * Inserts the widget in the wrapper creating bindings to its dimensions.
     *
     * @param widget widget to be added
     */
    public void addWidget(Pane widget) {
        widget.prefWidthProperty().bind(widgetBar.widthProperty());
        widget.prefHeightProperty().bind(widgetBar.heightProperty());
        widgetBar.getChildren().add(widget);
    }
}
