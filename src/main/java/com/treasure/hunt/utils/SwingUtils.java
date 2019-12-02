package com.treasure.hunt.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import javax.swing.*;

import static com.treasure.hunt.utils.JTSUtils.GEOMETRY_FACTORY;

public class SwingUtils {
    public static Point promptForPoint(String title, String message) {
        while (true) {
            JTextField xPositionTextField = new JTextField();
            JTextField yPositionTextField = new JTextField();
            final JComponent[] inputs = new JComponent[]{
                    new JLabel(message),
                    new JLabel("X Position"),
                    xPositionTextField,
                    new JLabel("Y Position"),
                    yPositionTextField
            };
            int result = JOptionPane.showConfirmDialog(null, inputs, title, JOptionPane.OK_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    double x = Double.parseDouble(xPositionTextField.getText());
                    double y = Double.parseDouble(yPositionTextField.getText());
                    return GEOMETRY_FACTORY.createPoint(new Coordinate(x, y));
                } catch (NumberFormatException e) {
                    JOptionPane.showConfirmDialog(null, "Please enter valid numbers", "Error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public static void infoPopUp(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
