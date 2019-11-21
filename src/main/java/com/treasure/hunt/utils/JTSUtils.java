package com.treasure.hunt.utils;

import com.treasure.hunt.strategy.hint.impl.AngleHint;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.*;

import javax.swing.*;

import static org.locationtech.jts.algorithm.Angle.angleBetweenOriented;
import static org.locationtech.jts.algorithm.Angle.normalizePositive;

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

    /**
     * Works only for angles <=Math.PI
     *
     * @param angleHint
     * @return
     */
    public static Coordinate middleOfAngleHint(AngleHint angleHint) {
        double betweenAngle = angleBetweenOriented(
                angleHint.getAnglePointRight().getCoordinate(),
                angleHint.getCenterPoint().getCoordinate(),
                angleHint.getAnglePointLeft().getCoordinate()
        );
        assert Math.PI >= betweenAngle && betweenAngle >= 0; // If the angle is <= PI
        double rightAngle = Angle.angle(angleHint.getCenterPoint().getCoordinate(),
                angleHint.getAnglePointRight().getCoordinate());
        double resultAngle = normalizePositive(rightAngle + betweenAngle * 1 / 2);
        double x = angleHint.getCenterPoint().getX() + (Math.cos(resultAngle) * 1);
        double y = angleHint.getCenterPoint().getY() + (Math.sin(resultAngle) * 1);
        return new Coordinate(x, y);
    }
}
