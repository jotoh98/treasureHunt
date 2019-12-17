package com.treasure.hunt.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

public class GeometryLine extends LineSegment {

    /**
     * Infinite lines have a positive infinite length.
     *
     * @return positive infinite length
     */
    @Override
    public double getLength() {
        return Double.POSITIVE_INFINITY;
    }

    /**
     * Wrapper for segment length
     *
     * @return segment length
     */
    public double getSegmentLength() {
        return super.getLength();
    }

    /**
     * Override the distance with the infinite-line distance function.
     *
     * @param p coordinate of the point to test the distance on
     * @return distance between the infinite line and the point
     */
    @Override
    public double distance(Coordinate p) {
        return super.distancePerpendicular(p);
    }

    /**
     * Explicit wrapper for the segments distance if needed.
     *
     * @param p coordinate of the point to test the distance on
     * @return distance between the line segment and the point
     */
    public double segmentDistance(Coordinate p) {
        return super.distance(p);
    }

    /**
     * Renames the string representation of GeometryLine
     *
     * @return string representation of GeometryLine
     */
    @Override
    public String toString() {
        return String.format("GEOMETRYLINE(%s %s, %s %s)", p0.x, p0.y, p1.x, p1.y);
    }
}
