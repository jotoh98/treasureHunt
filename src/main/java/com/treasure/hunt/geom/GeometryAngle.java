package com.treasure.hunt.geom;

import com.treasure.hunt.jts.AdvancedShapeWriter;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * The Angle is an geometric representation of an angle at a certain position.
 * It is defined by 3 {@link Coordinate}'s organized in the {@link LineString} in the following order:
 * <ul>
 *     <li>the right point,</li>
 *     <li>the center point and</li>
 *     <li>the left point.</li>
 * </ul>
 * These points are named after their counter-clockwise occurrence.
 */
public class GeometryAngle extends LineString implements Shapeable {

    /**
     * GeometryAngle constructor via three {@link Coordinate}s.
     *
     * @param factory The GeometryFactory suggested to create the <code>Angle</code>
     * @param right   the right angles arm end point
     * @param center  the central point of the angle
     * @param left    the left angles arm end point
     */
    public GeometryAngle(GeometryFactory factory, Coordinate right, Coordinate center, Coordinate left) {
        super(
                factory.getCoordinateSequenceFactory()
                        .create(new Coordinate[]{right, center, left}),
                factory
        );
    }

    /**
     * GeometryAngle with standard geometry factory.
     *
     * @param right  the right angles arm end point
     * @param center the central point of the angle
     * @param left   the left angles arm end point
     */
    public GeometryAngle(Coordinate right, Coordinate center, Coordinate left) {
        this(JTSUtils.GEOMETRY_FACTORY, right, center, left);
    }

    /**
     * GeometryAngle constructor via the central {@link Coordinate}, the start angle and the angles extend.
     *
     * @param factory The GeometryFactory suggested to create the <code>Angle</code>
     * @param center  the central point of the angle
     * @param start   starting angle relative to x-axis
     * @param extend  angle extend
     */
    public GeometryAngle(GeometryFactory factory, Coordinate center, double start, double extend) {
        this(
                factory,
                Vector2D.create(1, 0).rotate(start).translate(center),
                center,
                Vector2D.create(1, 0).rotate(start + extend).translate(center)
        );
    }

    /**
     * Set a new {@link Coordinate} at a certain position in the angles {@link org.locationtech.jts.geom.CoordinateSequence}
     *
     * @param i position of new coordinate
     * @param c new coordinate
     */
    private void setCoordinate(int i, Coordinate c) {
        points.getCoordinate(i).setX(c.getX());
        points.getCoordinate(i).setY(c.getY());
    }

    /**
     * Getter for right coordinate.
     *
     * @return right coordinate
     */
    public Coordinate getRight() {
        return points.getCoordinate(0);
    }

    /**
     * Setter for right coordinate.
     */
    public void setRight(Coordinate right) {
        setCoordinate(0, right);
    }

    /**
     * Getter for central coordinate.
     *
     * @return central coordinate
     */
    public Coordinate getCenter() {
        return points.getCoordinate(1);
    }

    /**
     * Setter for central coordinate.
     */
    public void setCenter(Coordinate center) {
        setCoordinate(1, center);
    }

    /**
     * Getter for left coordinate.
     *
     * @return left coordinate
     */
    public Coordinate getLeft() {
        return points.getCoordinate(2);
    }

    /**
     * Setter for left coordinate.
     */
    public void setLeft(Coordinate left) {
        setCoordinate(2, left);
    }

    /**
     * Get the vector representing the right arm of the angle.
     *
     * @return vector of right arm
     */
    public Vector2D rightVector() {
        return new Vector2D(getCenter(), getRight());
    }

    /**
     * Get the vector representing the left arm of the angle.
     *
     * @return vector of left arm
     */
    public Vector2D leftVector() {
        return new Vector2D(getCenter(), getLeft());
    }

    /**
     * Get the angle between the x-axis and the angles right arm.
     *
     * @return angle between x-axis and right arm
     */
    public double rightAngle() {
        return org.locationtech.jts.algorithm.Angle.angle(getCenter(), getRight());
    }

    /**
     * Get the angle between the x-axis and the angles left arm.
     *
     * @return angle between x-axis and left arm
     */
    private double leftAngle() {
        return org.locationtech.jts.algorithm.Angle.angle(getCenter(), getLeft());
    }

    /**
     * Get the {@link GeometryAngle}'s angle between the right and the left arm.
     *
     * @return the angles extend
     */
    public double extend() {
        double extend = leftAngle() - rightAngle();
        if (extend < 0) {
            extend += 2 * Math.PI;
        }
        return extend;
    }

    /**
     * Implementation of the {@link Shapeable#toShape(AdvancedShapeWriter)} method.
     * It draws lines from the center with a fixed length of 100 and an arc to signal
     * the amount and direction of the extend.
     *
     * @param shapeWriter shape factory
     * @return awt shape for a {@link GeometryAngle}
     */
    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        GeneralPath generalPath = new GeneralPath();

        generalPath.append(shapeWriter.createFixedLine(getCenter(), getLeft(), 100), false);
        generalPath.append(shapeWriter.createFixedLine(getCenter(), getRight(), 100), false);

        generalPath.append(shapeWriter.createArc(getCenter(), 100, rightAngle(), extend()), false);

        return generalPath;
    }

    /**
     * Override the Geometry types name.
     *
     * @return geometry type name
     */
    @Override
    public String getGeometryType() {
        return "GeometryAngle";
    }

    /**
     * Ask, whether a certain point lays in the view of the angle.
     * To decide if that requirement is met, the point must lay in between the two angle arms
     * and in the extends direction.
     *
     * @param coordinate the tested point
     * @return whether the point lays in the view
     */
    public boolean inView(Coordinate coordinate) {
        GeometryAngle testAngle = copy();
        testAngle.setRight(coordinate);
        double testExtend = testAngle.extend();
        return testExtend >= 0 && testExtend <= extend();
    }

    /**
     * Copy convenience method.
     *
     * @return copied {@link GeometryAngle} instance
     */
    @Override
    public GeometryAngle copy() {
        return new GeometryAngle(factory, getRight().copy(), getCenter().copy(), getLeft().copy());
    }

    /**
     * Enhance the string representation of the {@link GeometryAngle}.
     *
     * @return string representation of the {@link GeometryAngle}
     */
    @Override
    public String toString() {
        final Coordinate right = getRight();
        final Coordinate center = getCenter();
        final Coordinate left = getLeft();
        return String.format(
                "GeometryAngle(right=[%s, %s], center=[%s, %s], left=[%s, %s]",
                right.x, right.y,
                center.x, center.y,
                left.x, left.y
        );
    }
}
