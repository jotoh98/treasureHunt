package com.treasure.hunt.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;

/**
 * Adding a Geometry based Circle to the jts Geometry suite.
 *
 * @author jotoh
 * @see org.locationtech.jts.geom.Geometry
 */
public class Circle extends Polygon {
    double radius;

    public Circle(Coordinate coordinate, double radius, int numOfPoints, GeometryFactory geometryFactory) {
        super(null, null, geometryFactory);
        GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory(geometryFactory);
        geometricShapeFactory.setNumPoints(numOfPoints);
        geometricShapeFactory.setCentre(coordinate);
        geometricShapeFactory.setSize(radius * 2);
        Polygon circle = geometricShapeFactory.createCircle();
        this.shell = (LinearRing) circle.getExteriorRing();
    }

    public Circle(Coordinate coordinate, double radius, GeometryFactory geometryFactory) {
        this(coordinate, radius, 64, geometryFactory);
    }

    public static Circle UnitCircle(Coordinate coordinate, GeometryFactory geometryFactory) {
        return new Circle(coordinate, 1.0, geometryFactory);
    }
}
