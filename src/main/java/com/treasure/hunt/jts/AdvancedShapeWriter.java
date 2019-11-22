package com.treasure.hunt.jts;

import lombok.Setter;
import org.locationtech.jts.awt.PointTransformation;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.awt.geom.GeneralPath;

public class AdvancedShapeWriter extends ShapeWriter {

    /**
     * Holds the relative boundary of the rendered view.
     */
    @Setter
    private Coordinate upperLeftBoundary = new Coordinate();
    @Setter
    private Coordinate lowerRightBoundary = new Coordinate();

    public AdvancedShapeWriter(PointTransformation pointTransformation) {
        super(pointTransformation);
    }

    private double diameter() {
        double width = upperLeftBoundary.getX() - lowerRightBoundary.getX();
        double height = upperLeftBoundary.getY() - lowerRightBoundary.getY();
        return Math.sqrt(width * width + height * height);
    }

    public GeneralPath toShape(Ray ray) {
        GeneralPath rayPath = new GeneralPath();

        Coordinate start = ray.p0;
        Coordinate end = ray.p1;

        Vector2D rayVector = new Vector2D(start, end);

        rayVector = rayVector.multiply(this.diameter());

        rayPath.moveTo(start.getX(), start.getY());
        rayPath.lineTo(start.getX() + rayVector.getX(), start.getX() + rayVector.getY());
        return rayPath;
    }

    public GeneralPath toShape(Line line) {

        GeneralPath linePath = new GeneralPath();

        Coordinate start = line.p0;

        Vector2D vector2D = new Vector2D(line.p0, line.p1);

        Vector2D positiveRay = vector2D.multiply(this.diameter() / 2);
        Vector2D negativeRay = vector2D.multiply(-this.diameter() / 2);

        linePath.moveTo(start.getX() + negativeRay.getX(), start.getY() + negativeRay.getY());
        linePath.lineTo(start.getX() + positiveRay.getX(), start.getY() + positiveRay.getY());

        return linePath;
    }
}
