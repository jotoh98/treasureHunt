package com.treasure.hunt.geom;

import com.treasure.hunt.jts.PointTransformation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

public class Ray extends LineSegment implements Shapeable {
    public Ray(Coordinate p0, Coordinate p1) {
        super(p0, p1);
    }

    boolean interSectionInRay(Coordinate intersection) {
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

    private Line2D toLine(double scalar) {
        Vector2D rayVector = new Vector2D(p0, p1);
        rayVector.multiply(scalar);
        return new Line2D.Double(p0.x, p0.y, p0.x + rayVector.getX(), p0.y + rayVector.getY());
    }

    @Override
    public Shape toShape(PointTransformation pointTransformation) {
        GeneralPath path = new GeneralPath();

        Vector2D rayDirection = new Vector2D(p0, p1).normalize().multiply(pointTransformation.diameter());

        path.moveTo(p0.getX(), p0.getY());
        path.lineTo(p0.getX() + rayDirection.getX(), p0.getY() + rayDirection.getY());

        return path;
    }
}
