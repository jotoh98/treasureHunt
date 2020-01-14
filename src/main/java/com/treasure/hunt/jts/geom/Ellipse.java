package com.treasure.hunt.jts.geom;

import lombok.Getter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.GeometricShapeFactory;

/**
 * A Geometry based Ellipse for the jts Geometry suite.
 *
 * @author hassel
 * @see org.locationtech.jts.geom.Geometry
 */
public class Ellipse extends Polygon {

    /**
     * Center radius of the circle.
     */
    @Getter
    Coordinate center;

    /**
     * Radius not rotated collinear to the x-axis.
     */
    @Getter
    double radiusX;

    /**
     * Radius not rotated collinear to the y-axis.
     */
    @Getter
    private double radiusY;

    /**
     * Rotation relative to the x-axis.
     */
    @Getter
    double rotation;

    /**
     * Number of points to construct the shell with.
     */
    int numOfPoints;

    /**
     * Full arguments constructor
     *
     * @param center          center point of ellipse
     * @param radiusX         x-axis radius
     * @param radiusY         y-axis radius
     * @param rotation        rotation of ellipse (x-axis relative)
     * @param numOfPoints     amount of points forming the {@link LinearRing}
     * @param geometryFactory geometry factory
     */
    public Ellipse(Coordinate center, double radiusX, double radiusY, double rotation, int numOfPoints, GeometryFactory geometryFactory) {
        super(null, null, geometryFactory);
        this.center = center;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.rotation = rotation;
        this.numOfPoints = numOfPoints;
        updateShell();
    }

    /**
     * Standard constructor for 64-point polygon
     */
    public Ellipse(Coordinate center, double radiusX, double radiusY, double rotation, GeometryFactory geometryFactory) {
        this(center, radiusX, radiusY, rotation, 64, geometryFactory);
    }

    public Ellipse(Coordinate center, double radius, GeometryFactory geometryFactory) {
        this(center, radius, radius, 0d, 64, geometryFactory);
    }

    /**
     * Create a {@link Ellipse} with radii 1.0 without rotation
     *
     * @param coordinate      center of the circle
     * @param geometryFactory geometry factory for polygon
     * @return {@link Ellipse} with radii 1.0 without rotation
     */
    private static Ellipse unit(Coordinate coordinate, GeometryFactory geometryFactory) {
        return new Ellipse(coordinate, 1d, 1d, 0d, geometryFactory);
    }

    /**
     * Setter for the both of the ellipse radii.
     * Updates the shell.
     *
     * @param radius new radius
     */
    public void setRadius(double radius) {
        radiusX = radius;
        radiusY = radius;
        updateShell();
    }

    /**
     * Setter for the ellipses x-radius.
     * Updates the shell.
     *
     * @param r new x-radius
     */
    private void setRadiusX(double r) {
        radiusX = r;
        updateShell();
    }

    /**
     * Setter for the ellipses y-radius.
     * Updates the shell.
     *
     * @param r new y-radius
     */
    private void setRadiusY(double r) {
        radiusY = r;
        updateShell();
    }

    /**
     * Setter for the ellipse rotation.
     * Updates the shell.
     *
     * @param rotation new rotation
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
        updateShell();
    }

    /**
     * Setter for the polygon number of points.
     * Updates the shell.
     *
     * @param numOfPoints polygon number of points
     */
    public void setNumOfPoints(int numOfPoints) {
        this.numOfPoints = numOfPoints;
        updateShell();
    }

    public double getRadius() {
        return getRadiusX();
    }

    @Override
    public Geometry getBoundary() {
        //TODO: implement easy boundary rectangle
        return super.getBoundary();
    }

    /**
     * Create the {@link LinearRing} for the polygon representing the {@link Ellipse}.
     */
    protected void updateShell() {
        GeometricShapeFactory factory = new GeometricShapeFactory(getFactory());
        binToFactory(factory);
        shell = (LinearRing) factory.createEllipse().getExteriorRing();
    }

    private void binToFactory(GeometricShapeFactory factory) {
        factory.setCentre(center);
        factory.setNumPoints(numOfPoints);
        factory.setSize(2 * radiusY);
        factory.setWidth(2 * radiusX);
        factory.setRotation(rotation);
    }

    public Circle toCircle(boolean xRadius) {
        double radius = xRadius ? radiusX : radiusY;
        return new Circle(center, radius, numOfPoints, getFactory());
    }

    @Override
    public double getArea() {
        return Math.PI * radiusX * radiusY;
    }

    @Override
    public String toString() {
        return "Ellipse(" +
                "center=" + center +
                ", radiusX=" + radiusX +
                ", radiusY=" + radiusY +
                ", rotation=" + rotation +
                ')';
    }
}
