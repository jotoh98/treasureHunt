package com.treasure.hunt.view;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class WidgetBarController {

    @FXML
    private Pane widgetBar;

    public void initialize() {
        System.out.println("hi");
    }

    public void addWidget(Pane widget) {
        widget.prefWidthProperty().bind(widgetBar.widthProperty());
        widgetBar.getChildren().add(widget);
    }
}
