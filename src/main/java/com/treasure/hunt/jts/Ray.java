package com.treasure.hunt.jts;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

public class Ray extends LineSegment {
    public Ray(Coordinate p0, Coordinate p1) {
        super(p0, p1);
        double length = this.getLength();
        this.p1.setX(this.p0.getX() + p1.getX() / length);
        this.p1.setY(this.p0.getY() + p1.getY() / length);
    }

    protected boolean interSectionInRay(Coordinate intersection) {
        boolean rayDirectionX = p1.x - p0.x > 0;
        boolean rayDirectionY = p1.y - p0.y > 0;
        boolean testDirectionX = intersection.x - p0.x > 0;
        boolean testDirectionY = intersection.y - p0.y > 0;
        return rayDirectionX == testDirectionX && rayDirectionY == testDirectionY;
    }

    @Override
    public Coordinate intersection(LineSegment line) {
        Coordinate intersection = lineIntersection(line);

        if (intersection == null) {
            return null;
        } else if (interSectionInRay(intersection)) {
            return intersection;
        }

        return null;
    }

    @Override
    public double getLength() {
        return Double.POSITIVE_INFINITY;
    }
}
