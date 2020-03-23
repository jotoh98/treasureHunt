package com.treasure.hunt.jts.awt;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.awt.geom.Point2D;

/**
 * Transforms a source {@link Coordinate} from a strategy to a AWT {@link Point2D} considering a canvas offset
 * {@link Vector2D} and a viewport scale. These are supplied by a
 * scrolling event listener through zooming and dragging
 * actions. Furthermore, it manages the bounding box representing the visual frame of the canvas inside the
 * mathematical vector space. This boundary is used for certain rendering purposes.
 *
 * @author jotoh
 * @version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
public class PointTransformation implements org.locationtech.jts.awt.PointTransformation {

    private static final double INITIAL_SCALE = 25.0;
    public static final double MIN_SCALE = 1e-4;
    public static final double MAX_SCALE = 10;

    /**
     * The scale translates the source coordinates multiplicative in {@link PointTransformation#transform(Coordinate)}.
     */
    @Getter
    DoubleProperty scaleProperty = new SimpleDoubleProperty(1);
    /**
     * The offset translates the source coordinates additive in {@link PointTransformation#transform(Coordinate)}.
     */
    @Getter
    ObjectProperty<Vector2D> offsetProperty = new SimpleObjectProperty<>(new Vector2D(400, 400));

    @Setter
    @Getter
    private Vector2D boundarySize = new Vector2D(0, 0);

    /**
     * Main transform method necessary for extending {@link org.locationtech.jts.awt.PointTransformation}.
     *
     * @param src  The original coordinate
     * @param dest The point that we want to transform
     */
    @Override
    public void transform(Coordinate src, Point2D dest) {
        Coordinate coordinate = transform(src);
        dest.setLocation(
                coordinate.x,
                coordinate.y
        );
    }

    public double getScale() {
        return INITIAL_SCALE * scaleProperty.get();
    }

    public void setScale(double scale) {
        if (scale != 0) {
            scaleProperty.set(scale);
        }
    }

    public Vector2D getOffset() {
        return offsetProperty.get();
    }

    public void setOffset(Vector2D offset) {
        offsetProperty.set(offset);
    }

    /**
     * Transform method between {@link Coordinate}s.
     *
     * @param src the coordinate we want to transform
     * @return transformed coordinate
     */
    public Coordinate transform(Coordinate src) {
        return new Coordinate(transformX(src.x), transformY(src.y));
    }

    public double transformX(double x) {
        return getScale() * x + getOffset().getX();
    }

    public double transformY(double y) {
        return getScale() * -y + getOffset().getY();
    }

    public Coordinate revert(Coordinate src) {
        return revert(src.x, src.y);
    }

    public Coordinate revert(double x, double y) {
        return new Coordinate(revertX(x), revertY(y));
    }

    public double revertX(double x) {
        return (x - getOffset().getX()) / getScale();
    }

    public double revertY(double y) {
        return (y - getOffset().getY()) / -getScale();
    }


    /**
     * Scales the canvas and keeps a certain point centered.
     *
     * @param gamma factor of new scale
     * @param point scaling source point
     */
    public void scaleRelative(double gamma, Vector2D point) {
        double newScale = scaleProperty.get() * gamma;
        if (newScale < MIN_SCALE || newScale > MAX_SCALE) {
            newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));
            gamma = scaleProperty.get() / newScale;
            scaleOffset(gamma, point);
            setScale(newScale);
        }
        if (newScale > MIN_SCALE && newScale < MAX_SCALE) {
            scaleOffset(gamma, point);
            setScale(newScale);
        }
    }

    /**
     * Scale the offset relative keeping a point as scaling source.
     *
     * @param gamma factor of new scale
     * @param point scaling source point
     */
    public void scaleOffset(double gamma, Vector2D point) {
        double newScale = scaleProperty.get() * gamma;
        Vector2D direction = offsetProperty.get().subtract(point);

        setOffset(point.add(direction.multiply(newScale / scaleProperty.get())));
    }
}