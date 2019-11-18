package com.treasure.hunt.utils;

import org.locationtech.jts.geom.*;

public class JTSUtils {
    static GeometryFactory defaultGeometryFactory;

    public static GeometryFactory getDefaultGeometryFactory() {
        if (defaultGeometryFactory == null) {
            defaultGeometryFactory = new GeometryFactory();
        }
        return defaultGeometryFactory;
    }

    public static Point createPoint(double x, double y) {
        return getDefaultGeometryFactory().createPoint(new Coordinate(x, y));
    }

    public static LineString createLineString(Point A, Point B) {
        Coordinate[] coords = {A.getCoordinate(), B.getCoordinate()};
        return getDefaultGeometryFactory().createLineString(coords);
    }

    /**
     * Tests whether line line intersects with the linesegment linesegment
     *
     * @param line
     * @param linesegment
     * @return
     */
    public static Point lineLinesegmentIntersection(LineSegment line, LineSegment linesegment) {
        Point intersection = getDefaultGeometryFactory().createPoint(line.lineIntersection(linesegment));
        LineString lineSegString = createLineString(getDefaultGeometryFactory().createPoint(linesegment.p0),
                getDefaultGeometryFactory().createPoint(linesegment.p1));
        if (lineSegString.contains(intersection)) {
            return intersection;
        }
        return null;
    }
}
