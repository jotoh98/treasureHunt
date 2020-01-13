package com.treasure.hunt.geom;

import com.treasure.hunt.jts.AdvancedShapeWriter;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import java.awt.*;

/**
 * @author jotoh
 */
public class Line extends Ray {
    public Line(Coordinate p0, Coordinate p1) {
        super(p0, p1);
    }

    public Line(double x1, double y1, double x2, double y2) {
        this(new Coordinate(x1, y1), new Coordinate(x2, y2));
    }

    @Override
    public Coordinate intersection(LineSegment line) {
        return super.lineIntersection(line);
    }

    public boolean rightOfPivot(Coordinate coordinate) {
        return interSectionInRay(coordinate);
    }

    public boolean leftOfPivot(Coordinate coordinate) {
        return !rightOfPivot(coordinate);
    }

    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        double diameter = shapeWriter.getBoundary().diameter();

        Coordinate start = JTSUtils.coordinateInDistance(p0, p1, -diameter);
        Coordinate end = JTSUtils.coordinateInDistance(p0, p1, diameter);

        return shapeWriter.createLine(start, end);
    }
}
