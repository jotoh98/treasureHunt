package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.Angle;
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
 *     <li>the right point,</li>
 *     <li>the center point and</li>
 *     <li>the left point.</li>
 * </ul>
 * These points are understood in a counter-clockwise looking way from the center point.
 *
 * @author jotoh
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
        super(factory.getCoordinateSequenceFactory().create(new Coordinate[]{center, left, right}), factory);
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

    /** the Angle between the middle of the Hint and the x axis normalized to the range of [0 , 2*PI]
     *
     * @return the angle in radians
     */
    public double getNormalizedAngle(){
        double angle = Angle.angle(this.getCenter() , JTSUtils.middleOfGeometryAngle(this));
        if( angle < 0){
            angle += 2 * Math.PI;
        }
        return angle;
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

    public Ray leftRay(){
        return new Ray(getCenter().copy(),getLeft().copy());
    }

    public Ray rightRay(){
        return new Ray(getCenter().copy(),getRight().copy());
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

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public GeometryAngle copy() {
        return new GeometryAngle(factory, getRight().copy(), getCenter().copy(), getLeft().copy());
    }
}
