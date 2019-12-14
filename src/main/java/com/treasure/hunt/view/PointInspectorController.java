package com.treasure.hunt.view;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.game.Move;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;

@Slf4j
public class PointInspectorController {

    @FXML
    public TextField xCoordinateLabel;

    @FXML
    public TextField yCoordinateLabel;
    public ComboBox modeSelection;

    public void init(ObjectProperty<GameManager> gameManager) {
        gameManager.addListener(observable -> {

            if (observable == null) {
                return;
            }

            ObjectBinding<Move> moveObjectBinding = gameManager.get().lastMove();

            StringBinding xStringBinding = Bindings.createStringBinding(
                    () -> String.valueOf(moveObjectBinding.get().getTreasureLocation().getX()),
                    moveObjectBinding
            );

            StringBinding yStringBinding = Bindings.createStringBinding(
                    () -> String.valueOf(moveObjectBinding.get().getTreasureLocation().getY()),
                    moveObjectBinding
            );
            xCoordinateLabel.textProperty().bind(xStringBinding);
            yCoordinateLabel.textProperty().bind(yStringBinding);
        });
    }

    public void init(ObjectBinding<Point> point) {
        StringBinding xStringBinding = Bindings.createStringBinding(
                () -> String.valueOf(point.get().getX()),
                point
        );

        StringBinding yStringBinding = Bindings.createStringBinding(
                () -> String.valueOf(point.get().getY()),
                point
        );
        xCoordinateLabel.textProperty().bind(xStringBinding);
        yCoordinateLabel.textProperty().bind(yStringBinding);
    }
}

