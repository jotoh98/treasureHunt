package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.Shapeable;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;

/**
 * @author jotoh
 */
public class Ray extends LineSegment implements Shapeable {
    public Ray(Coordinate p0, Coordinate p1) {
        super(p0, p1);
        this.p1 = JTSUtils.normalizedCoordinate(p0, p1);
    }

    boolean interSectionInRay(Coordinate intersection) {
        Vector2D rayVector = new Vector2D(p0, p1);
        Vector2D testVector = new Vector2D(p0, intersection);
        return JTSUtils.signsEqual(rayVector, testVector);
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

    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        double diameter = shapeWriter.getBoundary().diameter();
        Coordinate end = JTSUtils.coordinateInDistance(p0, p1, diameter);
        return shapeWriter.createLine(p0, end);
    }
}
