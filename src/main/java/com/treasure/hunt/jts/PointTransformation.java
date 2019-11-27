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

@AllArgsConstructor
@NoArgsConstructor
public class PointTransformation implements org.locationtech.jts.awt.PointTransformation {

    /**
     * Holds the relative boundary of the rendered view.
     */
    @Setter
    @Getter
    private Rectangle boundary = new Rectangle();

    private Vector2D leftUpperBoundary = new Vector2D();
    private Vector2D rightLowerBoundary = new Vector2D();

    @Setter
    @Getter
    double scale = 1.0;

    @Getter
    Vector2D offset = new Vector2D(400, 400);

    public void setOffset(Vector2D offset) {
        this.offset = offset;
        boundary.setLocation((int) Math.floor(offset.getX()), (int) Math.floor(offset.getY()));
    }

    @Override
    public void transform(Coordinate src, Point2D dest) {
        dest.setLocation(
                .5 * (1 + scale) * (1 + scale) * src.x + offset.getX(),
                .5 * (1 + scale) * (1 + scale) * -src.y + offset.getY()
        );
    }

    public double diameter() {
        return Math.sqrt(boundary.width * boundary.width + boundary.height * boundary.height);
    }

    public Vector2D getLeftUpperBoundary() {
        return offset.negate().divide(scale);
    }

    public Vector2D getRightLowerBoundary(JPanel canvas) {
        Vector2D canvasDimension = new Vector2D(canvas.getWidth(), canvas.getHeight());
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

    public void setBoundarySize(int width, int height) {
        boundary.setSize(width, height);
    }
}
