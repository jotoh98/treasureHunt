package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.SwingUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import javax.swing.*;

/**
 * This is a type of {@link HideAndSeekHider},
 * which is controlled by the user.
 *
 * @author axel12
 */
public class UserControlledAngleHintHider implements HideAndSeekHider<AngleHint> {
    private Point treasureLocation;

    /**
     * @param movement the {@link Movement}, the {@link com.treasure.hunt.strategy.searcher.Searcher} did last
     * @return an {@link AngleHint} passed by the user
     */
    @Override
    public AngleHint move(Movement movement) {
        AngleHint angleHint = createAngleDialog(movement.getEndPoint());
        Coordinate[] angle = {angleHint.getAnglePointLeft().getCoordinate(), angleHint.getCenter().getCoordinate(), angleHint.getAnglePointRight().getCoordinate()};
        LineString lineString = JTSUtils.GEOMETRY_FACTORY.createLineString(angle);
        GeometryItem<LineString> hintGeometryItem = new GeometryItem<>(lineString, GeometryType.HINT_ANGLE);
        angleHint.addAdditionalItem(hintGeometryItem);
        return angleHint;
    }

    /**
     * @return the current treasure location, passed by the user
     */
    @Override
    public Point getTreasureLocation() {
        treasureLocation = SwingUtils.promptForPoint("Provide a treasure position", "...");
        return this.treasureLocation;
    }

    /**
     * @param middle the points, where the searcher stands
     * @return a {@link AngleHint}, passed by the user
     */
    private AngleHint createAngleDialog(Point middle) {
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
                    Point angleLeft = JTSUtils.GEOMETRY_FACTORY.createPoint(new Coordinate(x2, y2));
                    Point angleRight = JTSUtils.GEOMETRY_FACTORY.createPoint(new Coordinate(x, y));
                    if (!JTSUtils.pointInAngle(angleRight, middle, angleLeft, treasureLocation)) {
                        throw new UserControlledAngleHintHider.WrongAngleException("Treasure  Location not contained in angle");
                    }
                    if (!JTSUtils.angleDegreesSize(angleRight, middle, angleLeft, Math.PI)) {
                        throw new UserControlledAngleHintHider.WrongAngleException("Angle is bigger 180 degrees");
                    }
                    return new AngleHint(middle, angleLeft, angleRight);
                } catch (NumberFormatException e) {
                    JOptionPane.showConfirmDialog(null, "Please enter valid numbers", "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
                } catch (WrongAngleException e) {
                    JOptionPane.showConfirmDialog(null, "Please enter valid angle: " + e.getMessage(), "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Defines an {@link Exception}, where the passed angle were wrong.
     *
     * @author axel12
     */
    private static class WrongAngleException extends Exception {
        WrongAngleException(String message) {
            super(message);
        }
    }
}
