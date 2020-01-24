package com.treasure.hunt.view;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * @author jotoh
 */
public class WidgetBarController {
    @FXML
    private Pane widgetBar;

    public void addWidget(Pane widget) {
        widget.prefWidthProperty().bind(widgetBar.widthProperty());
        widget.prefHeightProperty().bind(widgetBar.heightProperty());
        widgetBar.getChildren().add(widget);
    }
}
