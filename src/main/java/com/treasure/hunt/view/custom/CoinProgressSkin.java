package com.treasure.hunt.view.custom;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;

import java.text.DecimalFormat;

public class CoinProgressSkin extends CoinLoaderSkin {

    private HBox hBox = new HBox();
    private DecimalFormat decimalFormat = new DecimalFormat("#%");

    public CoinProgressSkin(CoinProgress coinLoader) {
        super(coinLoader);
        hBox.getChildren().add(imageView);
        hBox.setAlignment(Pos.CENTER_LEFT);
        Label textField = new Label();
        textField.setTextOverrun(OverrunStyle.CLIP);
        textField.setMinWidth(30);
        hBox.getChildren().add(textField);

        textField.textProperty().bind(Bindings.createStringBinding(
                () -> decimalFormat.format(coinLoader.getProgress()),
                coinLoader.progressProperty()
        ));
    }

    @Override
    public Node getNode() {
        return hBox;
    }
}
