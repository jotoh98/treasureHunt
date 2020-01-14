package com.treasure.hunt.view.widget;

import javafx.scene.control.Label;

public class StatusMessageWidgetTextFlowController {
    public Label nameLabel;
    public Label messageLabel;

    public void setData(String name, String message) {
        nameLabel.setText(name);
        messageLabel.setText(message);
    }
}
