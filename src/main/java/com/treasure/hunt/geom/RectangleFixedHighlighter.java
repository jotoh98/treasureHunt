package com.treasure.hunt.geom;

import com.treasure.hunt.jts.AdvancedShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.awt.*;

public class RectangleFixedHighlighter extends RectangleVariableHighlighter implements Shapeable {
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

    public RectangleFixedHighlighter(Coordinate coordinate, double width, double height, GeometryFactory geometryFactory) {
        super(coordinate, width, height, geometryFactory);
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

    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        Point dest = new Point();

        shapeWriter.transform(coordinate, dest);

        Shape rectangle = new Rectangle.Double((dest.x - width / 2), (dest.y - height / 2), width, height);

        return rectangle;
    }
}
