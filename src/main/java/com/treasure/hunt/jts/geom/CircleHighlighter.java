package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.Shapeable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Non-scaling {@link Circle}, surrounding points to highlight them.
 *
 * @author dorianreineccius
 */
public class CircleHighlighter extends Circle implements Shapeable {
    public CircleHighlighter(Coordinate coordinate, double radius, int numOfPoints, GeometryFactory geometryFactory) {
        super(coordinate, radius, numOfPoints, geometryFactory);
    }

    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        Point dest = new Point();

        shapeWriter.transform(coordinate, dest);

        Shape circle = new Ellipse2D.Double(dest.x - radius, dest.y - radius, radius * 2, radius * 2);

        return circle;
    }
}
