package com.treasure.hunt.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import javax.swing.*;

public class JTSUtils {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public static GeometryFactory getDefaultGeometryFactory() {
        return GEOMETRY_FACTORY;
    }


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
                    return JTSUtils.getDefaultGeometryFactory().createPoint(new Coordinate(x, y));
                } catch (NumberFormatException e) {
                    JOptionPane.showConfirmDialog(null, "Please enter valid numbers", "Error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
