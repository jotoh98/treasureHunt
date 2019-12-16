package com.treasure.hunt.geom;

import com.treasure.hunt.jts.AdvancedShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * The Angle is an geometric representation of an angle at a certain position.
 * It is defined by 3 {@link Coordinate}'s ordered the named way in the {@link GeometryUtility#coordinates} array:
 * <ul>
 *     <li>the center point,</li>
 *     <li>the left point and</li>
 *     <li>the right point.</li>
 * </ul>
 * These points are understood in a counter-clockwise looking way from the center point.
 */
public class GeometryAngle extends LineString implements Shapeable {

    /**
     * Creates a new <code>Geometry</code> via the specified GeometryFactory.
     *
     * @param factory The GeometryFactory suggested to create the <code>Angle</code>
     */
    public GeometryAngle(GeometryFactory factory, Coordinate right, Coordinate center, Coordinate left) {
        super(
                factory.getCoordinateSequenceFactory()
                        .create(new Coordinate[]{center, left, right}),
                factory
        );
    }

    private void setCoordinate(int i, Coordinate c) {
        points.getCoordinate(i).setX(c.getX());
        points.getCoordinate(i).setY(c.getY());
    }

    public Coordinate getCenter() {
        return points.getCoordinate(0);
    }

    public void setCenter(Coordinate center) {
        setCoordinate(0, center);
    }

    public Coordinate getLeft() {
        return points.getCoordinate(1);
    }

    public void setLeft(Coordinate left) {
        setCoordinate(1, left);
    }

    public Coordinate getRight() {
        return points.getCoordinate(2);
    }

    public void setRight(Coordinate right) {
        setCoordinate(2, right);
    }


    public Vector2D rightVector() {
        return new Vector2D(getCenter(), getRight());
    }

    public Vector2D leftVector() {
        return new Vector2D(getCenter(), getLeft());
    }

    public double rightAngle() {
        return org.locationtech.jts.algorithm.Angle.angle(getCenter(), getRight());
    }

    private double leftAngle() {
        return org.locationtech.jts.algorithm.Angle.angle(getCenter(), getLeft());
    }

    public double extend() {
        double extend = leftAngle() - rightAngle();
        if (extend < 0) {
            extend += 2 * Math.PI;
        }
        return extend;
    }

    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        GeneralPath generalPath = new GeneralPath();

        generalPath.append(shapeWriter.createFixedLine(getCenter(), getLeft(), 100), false);
        generalPath.append(shapeWriter.createFixedLine(getCenter(), getRight(), 100), false);

        generalPath.append(shapeWriter.createArc(getCenter(), 100, rightAngle(), extend()), false);

        return generalPath;
    }

    @Override
    public String getGeometryType() {
        return "Angle";
    }

    public boolean inView(Coordinate coordinate) {
        GeometryAngle testAngle = copy();
        testAngle.setRight(coordinate);
        double testExtend = testAngle.extend();
        return testExtend >= 0 && testExtend <= extend();
    }

    @Override
    public GeometryAngle copy() {
        return new GeometryAngle(factory, getRight().copy(), getCenter().copy(), getLeft().copy());
    }
}
