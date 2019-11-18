package com.treasure.hunt.utils;

import org.locationtech.jts.geom.*;

import javax.swing.*;

public class JTSUtils {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public static GeometryFactory getDefaultGeometryFactory() {
        return GEOMETRY_FACTORY;
    }

    public static Point createPoint(double x, double y) {
        return getDefaultGeometryFactory().createPoint(new Coordinate(x, y));
    }

    public static LineString createLineString(Point A, Point B) {
        Coordinate[] coords = {A.getCoordinate(), B.getCoordinate()};
        return getDefaultGeometryFactory().createLineString(coords);
    }

    /**
     * Tests whether two {@link LineSegment} objects intersect.
     *
     * @param lineSegment1
     * @param lineSegment2
     * @return point of intersection, if the {@link LineSegment}'s intersect. null otherwise.
     */
    public static Point lineSegmentIntersection(LineSegment lineSegment1, LineSegment lineSegment2) {
        Point intersection = getDefaultGeometryFactory().createPoint(lineSegment1.lineIntersection(lineSegment2));
        LineString lineSegString = createLineString(getDefaultGeometryFactory().createPoint(lineSegment2.p0),
                getDefaultGeometryFactory().createPoint(lineSegment2.p1));
        if (lineSegString.contains(intersection)) {
            return intersection;
        }
        return null;
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
