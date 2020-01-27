package com.treasure.hunt.jts.awt;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Data
public class CanvasBoundary {
    /**
     * Link to the related point transformation.
     */
    PointTransformation transform;

    /**
     * Bound canvas width property.
     */
    private DoubleProperty canvasWidth = new SimpleDoubleProperty();

    /**
     * Bound canvas height property.
     */
    private DoubleProperty canvasHeight = new SimpleDoubleProperty();

    public CanvasBoundary(Canvas canvas, PointTransformation transform) {
        canvasWidth.bind(canvas.widthProperty());
        canvasHeight.bind(canvas.heightProperty());
        this.transform = transform;
    }

    public double getMinX() {
        return transform.revertX(0);
    }

    public double getMaxX() {
        return transform.revertX(canvasWidth.get());
    }

    public double getMinY() {
        return transform.revertY(canvasHeight.get());
    }

    public double getMaxY() {
        return transform.revertY(0);
    }

    public Vector2D getLeftUpper() {
        return new Vector2D(getMinX(), getMaxY());
    }

    public Vector2D getRightLower() {
        return new Vector2D(getMaxX(), getMinY());
    }

    /**
     * Get the length of the diagonal of the boundary rectangle
     *
     * @return length of diagonal
     */
    public double diameter() {
        return getRightLower().subtract(getLeftUpper()).length();
    }

    public double canvasDiameter() {
        final double h = canvasHeight.get();
        final double w = canvasWidth.get();
        return Math.sqrt(h * h + w * w);
    }

    public List<LineSegment> toLineSegments() {
        final ArrayList<LineSegment> lineSegments = new ArrayList<>();

        final List<Coordinate> coordinates = getCoordinates();

        int bound = coordinates.size();
        for (int outer = 0; outer < bound; outer++) {
            for (int inner = outer; inner < bound; inner++) {
                final Coordinate c1 = coordinates.get(outer);
                final Coordinate c2 = coordinates.get(inner);
                if (c1.x == c2.x ^ c1.y == c2.y) {
                    lineSegments.add(new LineSegment(c1, c2));
                }
            }
        }

        return lineSegments;
    }

    @NotNull
    public List<Coordinate> getCoordinates() {
        final ArrayList<Coordinate> coordinates = new ArrayList<>();
        Stream.of(getMinX(), getMaxX()).forEach(x ->
                Stream.of(getMinY(), getMaxY()).forEach(y ->
                        coordinates.add(new Coordinate(x, y))
                )
        );
        return coordinates;
    }

    public List<Coordinate> intersections(LineSegment lineSegment) {
        List<Coordinate> intersections = new ArrayList<>();

        toLineSegments().forEach(boundaryLine -> {
            final Coordinate intersection = lineSegment.intersection(boundaryLine);
            if (intersection != null) {
                intersections.add(intersection);
            }
        });

        return intersections;
    }
}
