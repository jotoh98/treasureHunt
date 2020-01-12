package com.treasure.hunt.view.widget;

import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.view.CanvasController;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.math.Vector2D;

import java.text.DecimalFormat;
import java.text.ParseException;

@Slf4j
public class ScaleController {
    final DecimalFormat percentageFormatter = new DecimalFormat("##.##%");
    public Slider slider;
    public TextField textField;
    public VBox wrapper;
    @Getter
    private DoubleProperty scale = new SimpleDoubleProperty(1);

    private CanvasController canvasController;

    /**
     * Behaviour of user entering a scale.
     */
    public void onEnter() {
        double cleanScale;

        try {
            cleanScale = percentageFormatter.parse(textField.getText()).doubleValue();
        } catch (ParseException e) {
            try {
                cleanScale = Double.parseDouble(textField.getText().replace(",", "."));
            } catch (NumberFormatException e1) {
                cleanScale = canvasController.getTransformation().getScaleProperty().get();
            }
        }

        canvasController.getTransformation().setScale(cleanScale);
        wrapper.requestFocus();
        textField.setText(percentageFormatter.format(canvasController.getTransformation().getScaleProperty().get()));
    }

    /**
     * Bind the transformation properties to the slider and text field.
     *
     * @param canvasController controller holding the {@link PointTransformation}
     */
    public void init(CanvasController canvasController) {
        this.canvasController = canvasController;

        final PointTransformation transformer = canvasController.getTransformation();

        transformer
                .getScaleProperty()
                .addListener((observable, oldValue, newValue) ->
                        textField.setText(percentageFormatter.format(newValue)
                        ));

        slider.valueProperty().bindBidirectional(transformer.getScaleProperty());

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!slider.isPressed() || oldValue.equals(0)) {
                return;
            }
            final Canvas canvas = canvasController.getCanvas();
            final Vector2D center = Vector2D.create(canvas.getWidth(), canvas.getHeight()).divide(2);

            transformer.scaleOffset(((double) newValue) / ((double) oldValue), center);
        });
    }
}
