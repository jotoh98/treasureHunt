package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.math.Vector2D;
import org.locationtech.jts.util.GeometricShapeFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

/**
 * Adding a {@link Polygon} based Circle to the {@link org.locationtech.jts} suite.
 *
 * @author jotoh
 * @see org.locationtech.jts.geom.Geometry
 */
public class Circle extends Coordinate implements Shapeable {

    @Getter
    private double radius;
    @Getter
    private int numPoints;

    /**
     * The constructor
     *
     * @param coordinate the center point of the circle.
     * @param radius     the radius of the circle.
     * @param numPoints  the number of points, the circle get after converting to a {@link Polygon}.
     */
    public Circle(Coordinate coordinate, double radius, int numPoints) {
        super(coordinate);
        this.radius = radius;
        this.numPoints = numPoints;
    }

    /**
     * The constructor
     *
     * @param coordinate the center point of the circle.
     * @param radius     the radius of the circle.
     */
    public Circle(Coordinate coordinate, double radius) {
        this(coordinate, radius, 64);
    }

    /**
     * Tests whether a coordinate lays inside the circle or not.
     *
     * @param c coordinate to test
     * @return whether a coordinate lays inside the circle or not
     */
    public boolean inside(Coordinate c) {
        return distance(c) <= radius;
    }

    /**
     * Test, whether the circle covers another circle.
     *
     * @param circle circle that may be covered.
     * @return whether the circle covers another circle or not
     */
    public boolean covers(Circle circle) {
        return distance(circle.getCenter()) + circle.radius <= radius;
    }

    /**
     * Project a coordinate on the edge of the circle.
     *
     * @param c coordinate to project
     * @return projected coordinate, may be center if projected point is center
     */
    public Coordinate project(Coordinate c) {
        return JTSUtils.coordinateInDistance(getCenter(), c, radius);
    }

    /**
     * Get central coordinate of circle.
     *
     * @return central coordinate
     */
    public Coordinate getCenter() {
        return new Coordinate(this);
    }

    /**
     * Generate a polygon from the circle.
     *
     * @return circular polygon
     */
    public Polygon toPolygon() {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(JTSUtils.GEOMETRY_FACTORY);
        shapeFactory.setCentre(getCenter());
        shapeFactory.setSize(2 * radius);
        shapeFactory.setNumPoints(numPoints);
        return shapeFactory.createCircle();
    }

    /**
     * Shape the circle as an awt ellipse.
     *
     * @param advancedShapeWriter shape writer for shape transformation
     * @return awt circle shape
     */
    @Override
    public Shape toShape(AdvancedShapeWriter advancedShapeWriter) {
        final GeneralPath generalPath = new GeneralPath();
        final Shape point = advancedShapeWriter.toShape(JTSUtils.createPoint(getCenter()));
        generalPath.append(point, false);
        final Coordinate translatedCenter = advancedShapeWriter.getPointTransformation().transform(getCenter());
        final double scaledRadius = advancedShapeWriter.getPointTransformation().getScale() * radius;
        final Ellipse2D.Double ellipse = new Ellipse2D.Double(translatedCenter.x - scaledRadius, translatedCenter.y - scaledRadius, 2 * scaledRadius, 2 * scaledRadius);
        generalPath.append(ellipse, false);
        return generalPath;
    }

    /**
     * Custom string serialization.
     *
     * @return string representation for circle
     */
    @Override
    public String toString() {
        return String.format("Circle(c: %s, r: %s)", getCenter(), radius);
    }

    /**
     * Override the coordinate copy.
     *
     * @return copied circle
     */
    @Override
    public Circle copy() {
        return new Circle(super.copy(), radius);
    }

    public Envelope getEnvelope() {
        final Vector2D corner = Vector2D.create(radius, radius);
        return new Envelope(
                corner.translate(getCenter()),
                corner.multiply(-1).translate(getCenter())
        );
    }

    /**
     * @param circle the {@link Circle} we want know test, whether it lies completely in this {@link Circle}
     * @return {@code true} if this contains {@code circle}. {@code false}, otherwise
     */
    public boolean contains(Circle circle) {
        return this.getRadius() >= (this.distance(circle) + circle.getRadius());
    }
}
