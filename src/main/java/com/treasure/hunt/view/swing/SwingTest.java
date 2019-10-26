package com.treasure.hunt.view.swing;

import com.treasure.hunt.jts.Circle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

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

    public static GeometryItem[] exampleGeometryItems() {
        GeometryFactory geometryFactory = new GeometryFactory();

        Circle c1 = new Circle(new Coordinate(100, 100), 50, geometryFactory);
        Circle c2 = new Circle(new Coordinate(150, 100), 50, geometryFactory);
        Polygon intersection = (Polygon) c1.intersection(c2);

        AffineTransformation affineTransformation = new AffineTransformation().translate(0.0, 20.0);

        intersection = (Polygon) affineTransformation.transform(intersection);

        Polygon c3 = (Polygon) affineTransformation.transform(c1);

        GeometryType intersectionType = new GeometryType(true, Color.red, Color.yellow, true, "");

        return new GeometryItem[]{
                new GeometryItem<>(c1, new GeometryType(true, Color.green)),
                new GeometryItem<>(c2, new GeometryType(true, Color.blue)),
                new GeometryItem<>(c3, intersectionType),
                new GeometryItem<>(intersection, intersectionType),
        };
    }

    public void paint(Graphics graphics) {
        /*Graphics2D graphics2D = (Graphics2D) graphics;
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

        geometricShapeFactory.setNumPoints(12);
        geometricShapeFactory.setCentre(new Coordinate(120, 120));
        geometricShapeFactory.setSize(40);

        Geometry circle = geometricShapeFactory.createCircle();

        Shape c_circle = shapeWriter.toShape(circle);

        graphics2D.draw(c_circle);

        Circle circle = new Circle(new Coordinate(120, 120), 10, geometryFactory);

        Shape new_circle = shapeWriter.toShape(circle);

        graphics2D.draw(new_circle);*/
    }
}
