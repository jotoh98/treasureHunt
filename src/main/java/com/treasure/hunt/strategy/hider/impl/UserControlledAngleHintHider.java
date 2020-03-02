package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.SwingUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import javax.swing.*;

import static org.locationtech.jts.algorithm.Angle.angleBetweenOriented;

/**
 * @author axel12
 */
public class UserControlledAngleHintHider implements HideAndSeekHider<AngleHint> {
    private Point treasureLocation;

    @Override
    public Point getTreasureLocation() {
        treasureLocation = SwingUtils.promptForPoint("Provide a treasure position", "...");
        return this.treasureLocation;
    }

    @Override
    public void init(Point searcherStartPosition) {

    }

    @Override
    public AngleHint move(SearchPath searchPath) {
        return createAngleDialog(searchPath.getLastPoint().getCoordinate());
    }

    private AngleHint createAngleDialog(Coordinate middle) {
        while (true) {
            JTextField xPositionTextField = new JTextField();
            JTextField yPositionTextField = new JTextField();
            JTextField xPositionTextField2 = new JTextField();
            JTextField yPositionTextField2 = new JTextField();
            final JComponent[] inputs = new JComponent[]{
                    new JLabel("Treasure: " + treasureLocation + "; Agent location: " + middle),
                    new JLabel("X Position Right"),
                    xPositionTextField,
                    new JLabel("Y Position Right"),
                    yPositionTextField,
                    new JLabel("X Position Left"),
                    xPositionTextField2,
                    new JLabel("Y Position Left"),
                    yPositionTextField2
            };
            int result = JOptionPane.showConfirmDialog(null, inputs, "Give angle params", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    double x = Double.parseDouble(xPositionTextField.getText());
                    double y = Double.parseDouble(yPositionTextField.getText());
                    double x2 = Double.parseDouble(xPositionTextField2.getText());
                    double y2 = Double.parseDouble(yPositionTextField2.getText());
                    Coordinate angleLeft = new Coordinate(x2, y2);
                    Coordinate angleRight = new Coordinate(x, y);
                    checkAngle(angleLeft, angleRight, middle);
                    return new AngleHint(angleRight, middle, angleLeft);
                } catch (NumberFormatException e) {
                    JOptionPane.showConfirmDialog(null, "Please enter valid numbers", "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
                } catch (WrongAngleException e) {
                    JOptionPane.showConfirmDialog(null, "Please enter valid angle: " + e.getMessage(), "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void checkAngle(Coordinate angleLeft, Coordinate angleRight, Coordinate middle) throws WrongAngleException {
        double angle = angleBetweenOriented(angleRight, middle, angleLeft);
        if (angle >= Math.PI || angle < 0) {
            throw new WrongAngleException("Angle is bigger 180 degrees");
        }
        double angleHintToTreasure = angleBetweenOriented(treasureLocation.getCoordinate(), middle, angleLeft);
        if (angleHintToTreasure > angle || angleHintToTreasure < 0) {
            throw new WrongAngleException("Treasure  Location not contained in angle");
        }
    }

    private static class WrongAngleException extends Exception {
        WrongAngleException(String message) {
            super(message);
        }
    }
}
