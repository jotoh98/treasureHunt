package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.CanvasBoundary;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.util.List;

/**
 * <p>The line is an extension of a {@link Ray} being infinitely long in both directions.</p>
 * <br>
 * <p>Like a {@link Ray}, it has an internal {@link LineSegment} representation with both {@link Coordinate}s being
 * points in the line one length-unit separated.</p>
 *
 * @author jotoh
 */
public class Line extends Ray {
    /**
     * Line constructor with two {@link Coordinate}s.
     *
     * @param p0 first coordinate defining the line
     * @param p1 second coordinate defining the line
     */
    public Line(Coordinate p0, Coordinate p1) {
        super(p0, p1);
    }

    /**
     * Line constructor with a base {@link Coordinate} and a directional {@link Vector2D}.
     *
     * @param p0        base point
     * @param direction direction vector
     */
    public Line(Coordinate p0, Vector2D direction) {
        super(p0, direction);
    }

    /**
     * Line constructor using 4 doubles forming 2 {@link Coordinate}s.
     *
     * @param x1 x-coordinate of first coordinate
     * @param y1 y-coordinate of first coordinate
     * @param x2 x-coordinate of second coordinate
     * @param y2 y-coordinate of second coordinate
     */
    public Line(double x1, double y1, double x2, double y2) {
        this(new Coordinate(x1, y1), new Coordinate(x2, y2));
    }

    /**
     * Intersect the infinite {@link Line} with a {@link LineSegment}.
     *
     * @param lineSegment segment to intersect ray with
     * @return intersection between infinite line and segment, if there is none: null
     */
    @Override
    public Coordinate intersection(LineSegment lineSegment) {
        final Coordinate coordinate = lineIntersection(lineSegment);
        if (coordinate != null && lineSegment.distance(coordinate) < 1e-7) {
            return coordinate;
        }
        return null;
    }

    /**
     * Intersect two infinite {@link Line}s.
     *
     * @param line infinite line to intersect this infinite line with
     * @return intersection between two infinite lines, if there is none: null
     */
    public Coordinate intersection(Line line) {
        return super.lineIntersection(line);
    }

    /**
     * Intersects a infinite {@link Line} with a {@link Ray}.
     *
     * @param ray ray to intersect this infinite line with
     * @return intersection between infinite line and ray, if there is none: null
     * @see Ray#intersection(Line)
     */
    public Coordinate intersection(Ray ray) {
        return ray.intersection(this);
    }

    /**
     * Convert the infinite line to a line shape, if it lays inside of the visual boundary.
     *
     * @param shapeWriter writer for shapes, holds the visual boundary
     * @return {@link Shape} of {@code this} line. {@code null}, if not visible.
     * @see JTSUtils#getBoundaryIntersections(CanvasBoundary, LineSegment)
     */
    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        final List<Coordinate> intersections = JTSUtils.getBoundaryIntersections(shapeWriter.getBoundary(), this);

        if (intersections.size() != 2) {
            return null;
        }

        return shapeWriter.createLine(intersections.get(0), intersections.get(1));
    }
}
