package com.treasure.hunt.jts.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;

/**
 * Adding a {@link Polygon} based Circle to the {@link org.locationtech.jts} suite.
 *
 * @author jotoh
 * @see org.locationtech.jts.geom.Geometry
 */
public class Circle extends Polygon {
    /**
     * The constructor
     *
     * @param coordinate      the center point of the circle.
     * @param radius          the radius of the circle.
     * @param numOfPoints     the number of points, the circle get after converting to a {@link Polygon}.
     * @param geometryFactory the {@link GeometryFactory}.
     */
    public Circle(Coordinate coordinate, double radius, int numOfPoints, GeometryFactory geometryFactory) {
        super(null, null, geometryFactory);
        GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory(geometryFactory);
        geometricShapeFactory.setNumPoints(numOfPoints);
        geometricShapeFactory.setCentre(coordinate);
        geometricShapeFactory.setSize(radius * 2);
        Polygon circle = geometricShapeFactory.createCircle();
        this.shell = (LinearRing) circle.getExteriorRing();
    }

    /**
     * The constructor
     *
     * @param coordinate      the center point of the circle.
     * @param radius          the radius of the circle.
     * @param geometryFactory the {@link GeometryFactory}.
     */
    public Circle(Coordinate coordinate, double radius, GeometryFactory geometryFactory) {
        this(coordinate, radius, 64, geometryFactory);
    }
}
