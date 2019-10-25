package com.treasure.hunt.ui.swing;

import com.treasure.hunt.ui.jts.Circle;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.util.GeometricShapeFactory;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;

public class SwingTest extends JPanel {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setVisible(true);

        GeometryPanel geometryPanel = new GeometryPanel();
        frame.setContentPane(geometryPanel);

        MetalLookAndFeel.setCurrentTheme(new TreasureHuntTheme());
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.updateComponentTreeUI(frame);
    }

    public void paint(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        ShapeWriter shapeWriter = new ShapeWriter();
        GeometryFactory geometryFactory = new GeometryFactory();
        GeometricShapeFactory geometricShapeFactory = new GeometricShapeFactory();

        Coordinate[] list = {new Coordinate(100, 100)};
        Point point = new org.locationtech.jts.geom.Point(new CoordinateArraySequence(list), geometryFactory);
        Coordinate[] t_coord = {
                new Coordinate(30, 30),
                new Coordinate(50, 50),
                new Coordinate(10, 50),
                new Coordinate(30, 30)
        };
        LineString ls = geometryFactory.createLineString(new Coordinate[]{new Coordinate(20, 20), new Coordinate(200, 20)});

        Polygon triangle = geometryFactory.createPolygon(new CoordinateArraySequence(t_coord));

        Shape shape = shapeWriter.toShape(ls);
        Shape c_point = shapeWriter.toShape(point);
        Shape c_triangle = shapeWriter.toShape(triangle);

        graphics2D.draw(shape);
        graphics2D.draw(c_point);
        graphics2D.draw(c_triangle);

        /*geometricShapeFactory.setNumPoints(12);
        geometricShapeFactory.setCentre(new Coordinate(120, 120));
        geometricShapeFactory.setSize(40);

        Geometry circle = geometricShapeFactory.createCircle();

        Shape c_circle = shapeWriter.toShape(circle);

        graphics2D.draw(c_circle);*/

        Circle circle = new Circle(new Coordinate(120, 120), 10, geometryFactory);

        Shape new_circle = shapeWriter.toShape(circle);

        graphics2D.draw(new_circle);
    }

    public void drawGeometry(Graphics2D graphics2D, Geometry geometry) {

    }
}
