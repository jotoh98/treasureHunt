package com.treasure.hunt.jts;

import com.treasure.hunt.jts.awt.PointTransformation;
import com.treasure.hunt.utils.JTSUtils;
import javafx.scene.canvas.Canvas;
import lombok.Data;
import org.locationtech.jts.math.Vector2D;

@Data
public class CanvasBoundary {
    /**
     * Link to the related point transformation.
     */
    PointTransformation transformation;
    /**
     * Upper left corner of the rectangle.
     */
    private Vector2D offset;
    /**
     * Width and height of the rectangle.
     */
    private Vector2D diagonal;
    /**
     * Link to the addressed canvas.
     */
    private Canvas canvas;

    public CanvasBoundary(Canvas canvas, PointTransformation transformation) {
        setCanvas(canvas);
        setTransformation(transformation);
    }

    public double getMinX() {
        return offset.getX();
    }

    public double getMaxX() {
        return offset.add(diagonal).getX();
    }

    public double getMinY() {
        return offset.getY();
    }

    public double getMaxY() {
        return offset.add(diagonal).getY();
    }

    /**
     * Get the length of the diagonal of the boundary rectangle
     *
     * @return length of diagonal
     */
    public double diameter() {
        return diagonal.length();
    }

    public Vector2D getCanvasDimensions() {
        return Vector2D.create(canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Update the rectangles dimension and offset.
     */
    public void update() {
        final double scale = transformation.getScale();
        if (scale <= 0) {
            return;
        }

        setDiagonal(getCanvasDimensions().divide(scale));

        setOffset(JTSUtils.negateX(transformation.getOffset()));
    }
}
