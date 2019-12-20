package com.treasure.hunt.geom;

import com.treasure.hunt.jts.AdvancedShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.awt.*;

public class RectangleHighlighter extends Polygon implements Shapeable {

    /**
     * Coordinate of the upper left corner.
     */
    public final Coordinate coordinate;
    public final double width;
    public final double height;

    public RectangleHighlighter(Coordinate coordinate, double width, double height, GeometryFactory geometryFactory) {
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

    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        Point dest = new Point();

        shapeWriter.transform(coordinate, dest);

        Shape rectangle = new Rectangle((int) (dest.x - width / 2), (int) (dest.y - height / 2), (int) width, (int) height);

        return rectangle;
    }
}
