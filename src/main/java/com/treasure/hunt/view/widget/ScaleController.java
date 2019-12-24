package com.treasure.hunt.view.widget;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import lombok.Getter;

import java.text.DecimalFormat;

public class ScaleController {
    final DecimalFormat df = new DecimalFormat("##.##%");
    public Slider slider;
    public TextField textField;
    public HBox wrapper;
    @Getter
    private DoubleProperty scale = new SimpleDoubleProperty(1);

    public void initialize() {
        scale.bind(sliderProperty());
        textField.textProperty().bind(
                Bindings.createStringBinding(
                        () -> df.format(scale.get() * 100),
                        scale
                )
        );
        slider.valueProperty().bind(scale);
    }

    private DoubleBinding sliderProperty() {
        //TODO: logarithmic scale
        return Bindings.createDoubleBinding(() -> slider.getValue(), slider.valueProperty());
    }

    public void scaleOut() {
        scale.set(scale.get() - .1d);
    }

    public void scaleIn() {
        scale.set(scale.get() + .1d);
    }

    public void onDrag() {
    }

    public void onEnter() {
        final String text = textField.getText();
        double cleanScale = Double.parseDouble(text.replace('%', ' '));

        if (text.contains("%")) {
            cleanScale /= 100d;
        }

        scale.set(cleanScale);
        wrapper.requestFocus();
        textField.setText(df.format(scale.get()));
    }
}
