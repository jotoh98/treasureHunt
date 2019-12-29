package com.treasure.hunt.view.widget;

import com.treasure.hunt.view.CanvasController;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import lombok.Getter;

import java.text.DecimalFormat;
import java.text.ParseException;

public class ScaleController {
    final DecimalFormat df = new DecimalFormat("##.##%");
    public Slider slider;
    public TextField textField;
    public HBox wrapper;
    @Getter
    private DoubleProperty scale = new SimpleDoubleProperty(1);

    private CanvasController canvasController;

    public void onEnter() {
        double cleanScale = canvasController.getTransformation().getScaleProperty().get();
        try {
            cleanScale = (double) df.parse(textField.getText());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        canvasController.getTransformation().setScale(cleanScale);
        wrapper.requestFocus();
        textField.setText(df.format(canvasController.getTransformation().getScaleProperty().get()));
    }

    public void init(CanvasController canvasController) {
        this.canvasController = canvasController;

        canvasController
                .getTransformation()
                .getScaleProperty()
                .addListener((observable, oldValue, newValue) -> {
                    textField.setText(df.format(newValue));
                    slider.setValue((double) newValue);
                });

        slider.valueProperty().bindBidirectional(canvasController.getTransformation().getScaleProperty());
    }
}
