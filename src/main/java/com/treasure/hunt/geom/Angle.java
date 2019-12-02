package com.treasure.hunt.geom;

import com.treasure.hunt.jts.PointTransformation;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

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
@Data
public class Angle extends LineString implements Shapeable {

    Ray leftRay;
    Ray rightRay;

    /**
     * Creates a new <code>Geometry</code> via the specified GeometryFactory.
     *
     * @param factory The GeometryFactory suggested to create the <code>Angle</code>
     */
    public Angle(GeometryFactory factory, Coordinate center, Coordinate left, Coordinate right) {
        super(
                factory.getCoordinateSequenceFactory()
                        .create(new Coordinate[]{center, left, right}),
                factory
        );

        leftRay = new Ray(center, left);
        rightRay = new Ray(center, right);
    }

    public Coordinate getCenter() {
        return leftRay.p0;
    }

    public void setCenter(Coordinate center) {
        leftRay.p0 = center;
        rightRay.p0 = center;
    }

    public Coordinate getLeft() {
        return leftRay.p1;
    }

    public void setLeft(Coordinate left) {
        leftRay.p1 = left;
    }

    public Coordinate getRight() {
        return rightRay.p1;
    }

    public void setRight(Coordinate right) {
        rightRay.p1 = right;
    }

    public double rightAngle() {
        return org.locationtech.jts.algorithm.Angle.angle(getCenter(), getRight());
    }

    public double leftAngle() {
        return org.locationtech.jts.algorithm.Angle.angle(getCenter(), getLeft());
    }

    public double toRadians() {
        return leftAngle() - rightAngle();
    }

    @Override
    public Shape toShape(PointTransformation pointTransformation) {
        GeneralPath generalPath = new GeneralPath();

        Point2D destMiddle = new Point2D.Double();
        pointTransformation.transform(getCenter(), destMiddle);

        double middleX = destMiddle.getX();
        double middleY = destMiddle.getY();

        Vector2D leftVector = new Vector2D(getCenter(), getLeft());
        Vector2D rightVector = new Vector2D(getCenter(), getRight());

        leftVector = leftVector.normalize().multiply(100);
        rightVector = rightVector.normalize().multiply(100);

        generalPath.moveTo(
                middleX + leftVector.getX(),
                middleY - leftVector.getY()
        );
        generalPath.lineTo(
                middleX,
                middleY
        );
        generalPath.lineTo(
                middleX + rightVector.getX(),
                middleY - rightVector.getY()
        );

        double extend = leftAngle() - rightAngle();

        if (leftAngle() < rightAngle()) {
            extend += 2 * Math.PI;
        }

        Arc2D arc = new Arc2D.Double(
                middleX - 50,
                middleY - 50,
                100,
                100,
                Math.toDegrees(rightAngle()),
                Math.toDegrees(extend),
                Arc2D.OPEN
        );

        generalPath.append(arc, false);

        return generalPath;
    }

    @Override
    public String getGeometryType() {
        return "Angle";
    }
}
