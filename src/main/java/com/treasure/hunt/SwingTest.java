package com.treasure.hunt;

import com.treasure.hunt.jts.Circle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

public class SwingTest {
    public static GeometryItem[] exampleGeometryItems() {
        GeometryFactory geometryFactory = new GeometryFactory();

        Circle c1 = new Circle(new Coordinate(100, 100), 50, geometryFactory);
        Circle c2 = new Circle(new Coordinate(150, 100), 50, geometryFactory);
        Polygon intersection = (Polygon) c1.intersection(c2);

        AffineTransformation affineTransformation = new AffineTransformation().translate(0.0, 20.0);

        intersection = (Polygon) affineTransformation.transform(intersection);

        Polygon originPoint = (Polygon) affineTransformation.transform(c1);

        Point p1 = geometryFactory.createPoint(new Coordinate(0, 0));
        return new GeometryItem[]{
                new GeometryItem<>(c1),
                new GeometryItem<>(c2),
                new GeometryItem<>(originPoint),
                new GeometryItem<>(intersection),
                new GeometryItem<>(p1)
        };
    }
}
