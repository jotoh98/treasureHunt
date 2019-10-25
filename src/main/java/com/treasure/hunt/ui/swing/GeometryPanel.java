package com.treasure.hunt.ui.swing;

import com.treasure.hunt.ui.jts.Circle;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.util.GeometricShapeFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class GeometryPanel extends JPanel {
    public GeometryPanel() {
        super();
    }

    public void paint(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        GeometryFactory geometryFactory = new GeometryFactory();
        AffineTransform affineTransform = new AffineTransform();

        Circle c1 = new Circle(new Coordinate(100, 100), 50, geometryFactory);
        Circle c2 = new Circle(new Coordinate(150, 100), 50, geometryFactory);
        Polygon intersection = (Polygon) c1.intersection(c2);

        AffineTransformation affineTransformation = new AffineTransformation().translate(0.0, 20.0);

        intersection = (Polygon) affineTransformation.transform(intersection);

        drawShape(graphics2D, new Geometry[]{c1, c2, intersection}, geometryFactory);
    }

    public void drawShape(Graphics2D graphics2D, Geometry[] geometries, GeometryFactory geometryFactory) {
        for (Geometry geometry : geometries)
            drawShape(graphics2D, geometry, geometryFactory);
    }

    public void drawShape(Graphics2D graphics2D, Geometry geometry, GeometryFactory geometryFactory) {
        GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory(geometryFactory);
        ShapeWriter shapeWriter = new ShapeWriter();
        graphics2D.setColor(new Color(0xFF3D27));
        graphics2D.draw(shapeWriter.toShape(geometry));
    }

}
