package com.treasure.hunt.geom;

import com.treasure.hunt.jts.PointTransformation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.geom.GeneralPath;

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
    public Shape toShape(PointTransformation pointTransformation) {
        GeneralPath linePath = new GeneralPath();

        Coordinate start = p0;

        Vector2D vector2D = new Vector2D(p0, p1);

        Vector2D positiveRay = vector2D.normalize().multiply(pointTransformation.diameter());
        Vector2D negativeRay = vector2D.normalize().multiply(-pointTransformation.diameter());

        linePath.moveTo(start.getX() + negativeRay.getX(), start.getY() + negativeRay.getY());
        linePath.lineTo(start.getX() + positiveRay.getX(), start.getY() + positiveRay.getY());

        return linePath;
    }
}
