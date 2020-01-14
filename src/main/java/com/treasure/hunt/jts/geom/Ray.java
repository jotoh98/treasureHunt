package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.util.List;

/**
 * <p>The Ray class is a line segment with one side being infinitely long.</p>
 * <br>
 * <p>It has an internal {@link LineSegment} representation with one {@link Coordinate} being
 * the start of the ray and the second {@link Coordinate} being a point one length-unit away
 * from the start in the direction of the ray.</p>
 *
 * @author jotoh
 */
public class Ray extends LineSegment implements Shapeable {
    /**
     * Ray constructor from two coordinates.
     *
     * @param start  ray starting coordinate
     * @param target ray direction coordinate
     */
    public Ray(Coordinate start, Coordinate target) {
        super(start, target);
        this.p1 = JTSUtils.normalizedCoordinate(start, target);
    }

    /**
     * Ray constructor from a start coordinate and a direction vector.
     *
     * @param start     ray starting coordinate
     * @param direction ray direction vector
     */
    public Ray(Coordinate start, Vector2D direction) {
        this(start, direction.translate(start));
    }

    /**
     * Checks, if a given {@link Coordinate} lays in the ray.
     *
     * @param coordinate coordinate to check
     * @return whether point lays in ray or not
     */
    public boolean inRay(Coordinate coordinate) {
        if (!inLine(coordinate)) {
            return false;
        }
        Vector2D rayVector = new Vector2D(p0, p1);
        Vector2D testVector = new Vector2D(p0, coordinate);
        return JTSUtils.signsEqual(rayVector, testVector);
    }

    /**
     * Intersect ray with {@link LineSegment}
     *
     * @param line segment to intersect ray with
     * @return intersection between segment and ray, if there is none: null
     */
    @Override
    public Coordinate intersection(LineSegment line) {
        Coordinate intersection = lineIntersection(line);

        if (intersection != null && inRay(intersection) && JTSUtils.inSegment(line, intersection)) {
            return intersection;
        }

        return null;
    }

    /**
     * Intersect ray with {@link Ray}
     *
     * @param ray ray to intersect ray with
     * @return intersection between two ray, if there is none: null
     */
    public Coordinate intersection(Ray ray) {
        final Coordinate intersection = lineIntersection(ray);

        if (intersection != null && inRay(intersection) && ray.inRay(intersection)) {
            return intersection;
        }

        return null;
    }

    /**
     * Intersect ray with {@link Line}
     *
     * @param line infinite line to intersect ray with
     * @return intersection between ray and infinite line, if there is none: null
     */
    public Coordinate intersection(Line line) {
        Coordinate intersection = lineIntersection(line);

        if (intersection != null && inRay(intersection)) {
            return intersection;
        }

        return null;
    }

    public boolean intersects(LineSegment line) {
        return intersection(line) != null;
    }

    /**
     * Get the rays length, which is positive infinite by definition.
     *
     * @return the rays length = positive infinite
     */
    @Override
    public double getLength() {
        return Double.POSITIVE_INFINITY;
    }

    /**
     * Convert the ray to a line shape, if it lays inside of the visual boundary.
     *
     * @param shapeWriter writer for shapes, holds the visual boundary
     * @return line shape, or if not visible, null
     */
    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        final List<Coordinate> intersections = JTSUtils.getBoundaryIntersections(shapeWriter.getBoundary(), this);

        if (intersections.size() == 0) {
            return null;
        } else if (intersections.size() == 1) {
            return shapeWriter.createLine(p0, intersections.get(0));
        }

        return shapeWriter.createLine(intersections.get(0), intersections.get(1));
    }

    public boolean inLine(Coordinate c) {
        return JTSUtils.inLine(this, c);
    }
}
