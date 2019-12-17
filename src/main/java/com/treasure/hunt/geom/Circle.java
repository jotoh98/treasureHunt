package com.treasure.hunt.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class Circle extends Ellipse {
    public Circle(Coordinate center, double radius, int numOfPoints, GeometryFactory factory) {
        super(center, radius, radius, 0d, numOfPoints, factory);
    }

    public Circle(Coordinate center, double radius, GeometryFactory factory) {
        super(center, radius, radius, 0d, factory);

    }

    /**
     * Add more accurate covers methods for circle-context.
     *
     * @param g testing {@link Geometry} item
     * @return whether Circle covers g or not
     */
    @Override
    public boolean covers(Geometry g) {
        if (g instanceof Point) {
            return center.distance(g.getCoordinate()) <= radiusX;
        }
        return super.covers(g);
    }


}
