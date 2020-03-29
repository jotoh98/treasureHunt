package com.treasure.hunt.jts.geom;

import com.treasure.hunt.utils.JTSUtils;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.math.Vector2D;
import org.locationtech.jts.util.GeometricShapeFactory;

import java.util.Collections;
import java.util.List;

/**
 * Adding a {@link Polygon} based Circle to the {@link org.locationtech.jts} suite.
 *
 * @author jotoh
 * @see org.locationtech.jts.geom.Geometry
 */
public class Circle extends Coordinate {

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

    public List<Coordinate> intersection(LineSegment l) {
        double dx = l.p1.x - l.p0.x;
        double dy = l.p1.y - l.p0.y;
        double dr = Vector2D.create(dx, dy).length();
        double D = l.p0.x * l.p1.y - l.p1.y * l.p0.y;

        double discriminant = radius * radius * dr * dr - D * D;

        if (discriminant < 0) {
            return Collections.emptyList();
        }
        //TODO: implement intersection
        return null;
    }

    public boolean contains(Coordinate c) {
        return distance(c) <= radius;
    }

    public double getArea() {
        return Math.PI * radius * radius;
    }

    public double distance(Coordinate c) {
        return getCenter().distance(c);
    }

    public double outerDistance(Coordinate c) {
        return distance(c) - radius;
    }

    public Coordinate intersection(Coordinate c) {
        double distance = getCenter().distance(c);
        if (distance < getRadius()) {
            return null;
        }
        if (distance == getRadius()) {
            return c;
        }
        return JTSUtils.coordinateInDistance(getCenter(), c, radius);
    }

    public Coordinate getCenter() {
        return new Coordinate(this);
    }


    public Polygon toPolygon() {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(JTSUtils.GEOMETRY_FACTORY);
        shapeFactory.setCentre(getCenter());
        shapeFactory.setSize(radius);
        shapeFactory.setNumPoints(numPoints);
        return shapeFactory.createCircle();
    }

    public boolean covers(Point p) {

    }
}
