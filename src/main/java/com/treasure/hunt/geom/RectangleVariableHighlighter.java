package com.treasure.hunt.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

public class RectangleVariableHighlighter extends Polygon {
    /**
     * Coordinate of the upper left corner.
     */
    public final Coordinate coordinate;
    /**
     * Width of the rectangle.
     */
    public final double width;
    /**
     * Height of the rectangle.
     */
    public final double height;

    public RectangleVariableHighlighter(Coordinate coordinate, double width, double height, GeometryFactory geometryFactory) {
        super(null, null, geometryFactory);
        Coordinate[] coords = new Coordinate[]{
                coordinate,
                new Coordinate(coordinate.x + width, coordinate.y),
                new Coordinate(coordinate.x + width, coordinate.y - height),
                new Coordinate(coordinate.x, coordinate.y - height),
                coordinate
        };
        this.shell = geometryFactory.createLinearRing(coords);
        this.coordinate = coordinate;
        this.width = width;
        this.height = height;
    }
}
