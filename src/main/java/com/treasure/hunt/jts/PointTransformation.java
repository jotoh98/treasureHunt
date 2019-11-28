package com.treasure.hunt.jts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Transforms a source {@link Coordinate} from a strategy to a AWT {@link Point2D} considering a canvas offset
 * {@link Vector2D} and a viewport scale. These are supplied by a
 * {@link com.treasure.hunt.view.swing.CanvasMouseListener} through zooming and dragging
 * actions. Furthermore, it manages the bounding box representing the visual frame of the canvas inside the
 * mathematical vector space of the algorithm logic for rendering purposes.
 *
 * @version 1.0
 * @see com.treasure.hunt.view.swing.CanvasMouseListener
 */
@AllArgsConstructor
@NoArgsConstructor
public class PointTransformation implements org.locationtech.jts.awt.PointTransformation {

    @Setter
    @Getter
    private Vector2D canvasDiameter = new Vector2D();

    /**
     * Holds the relative boundary of the rendered view.
     */
    private Vector2D leftUpperBoundary = new Vector2D();
    private Vector2D rightLowerBoundary = new Vector2D();

    @Setter
    @Getter
    double scale = 1.0;

    @Getter
    Vector2D offset = new Vector2D(400, 400);

    public void setOffset(Vector2D offset) {
        this.offset = offset;
    }

    @Override
    public void transform(Coordinate src, Point2D dest) {
        dest.setLocation(
                scale * src.x + offset.getX(),
                scale * -src.y + offset.getY()
        );
    }

    public double diameter() {
        return getMainDiagonalVector().length();
    }

    public Vector2D getLeftUpperBoundary() {
        Vector2D normalisedOffset = offset.divide(scale);
        return new Vector2D(-normalisedOffset.getX(), normalisedOffset.getY());
    }

    public Vector2D getRightLowerBoundary(JPanel canvas) {
        Vector2D canvasDimension = new Vector2D(canvas.getWidth(), canvas.getHeight());
        Vector2D leftUpperBoundary = getLeftUpperBoundary();
        return canvasDimension.subtract(offset).divide(scale);
    }

    public Rectangle getBoundaryRect(JPanel canvas) {
        Vector2D leftUpperBoundary = getLeftUpperBoundary();
        Vector2D rightLowerBoundary = getRightLowerBoundary(canvas);

        return new Rectangle(
                (int) leftUpperBoundary.getX(),
                (int) leftUpperBoundary.getY(),
                (int) rightLowerBoundary.getX(),
                (int) rightLowerBoundary.getY()
        );
    }

    public void updateBoundary(int canvasWidth, int canvasHeight) {
        setBoundaryLocation(-1 * offset.getX(), offset.getY());
        setBoundarySize(canvasWidth / scale, canvasHeight / scale);
    }

    public void setBoundarySize(Vector2D diagonal) {
        rightLowerBoundary = leftUpperBoundary.add(diagonal);
    }

    public void setBoundarySize(double width, double height) {
        setBoundarySize(new Vector2D(width, height));
    }

    public void setBoundaryLocation(double x, double y) {
        setBoundaryLocation(new Vector2D(x, y));
    }

    public void setBoundaryLocation(Vector2D location) {
        Vector2D diagonalVector = getMainDiagonalVector();
        leftUpperBoundary = location;
        rightLowerBoundary = leftUpperBoundary.add(diagonalVector);

    }

    public Vector2D getMainDiagonalVector() {
        return leftUpperBoundary.subtract(rightLowerBoundary);
    }


}
