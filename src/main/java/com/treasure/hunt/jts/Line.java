package com.treasure.hunt.jts;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

public class Line extends Ray {
    public Line(Coordinate p0, Coordinate p1) {
        super(p0, p1);
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
}
