package com.treasure.hunt.geom;

import com.treasure.hunt.jts.AdvancedShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class CircleHighlighter extends Circle implements Shapeable {
    public CircleHighlighter(Coordinate coordinate, double radius, int numOfPoints, GeometryFactory geometryFactory) {
        super(coordinate, radius, numOfPoints, geometryFactory);
    }

    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        // dest - radius
        Point dest = new Point();

        shapeWriter.transform(coordinate, dest);

        Shape circle = new Ellipse2D.Double(dest.x - radius, dest.y - radius, radius * 2, radius * 2);

        return circle;
    }
}
