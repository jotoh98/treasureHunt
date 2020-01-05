package com.treasure.hunt.jts;

import com.treasure.hunt.utils.JTSUtils;
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

    public void setScale(double scale) {
        if (scale != 0) {
            scaleProperty.set(scale);
        }
    }

    public void setOffset(Vector2D offset) {
        offsetProperty.set(offset);
    }

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


    /**
     * Transform method between {@link Coordinate}'s.
     *
     * @param src the coordinate we want to transform
     * @return transformed coordinate
     */
    public Coordinate transform(Coordinate src) {
        return new Coordinate(scaleProperty.get() * src.x + offsetProperty.get().getX(), scaleProperty.get() * -src.y + offsetProperty.get().getY());
    }

    /**
     * Method to retrieve the Vector from the upper left boundary point to the lower right boundary point.
     *
     * @return vector from upper left to lower right boundary point
     */
    private Vector2D getMainDiagonalVector() {
        return getUpperLeftBoundary().subtract(getLowerRightBoundary());
    }

    /**
     * Get the length of the diagonal of the boundary rectangle
     *
     * @return length of main diagonal
     */
    public double diameter() {
        return getMainDiagonalVector().length();
    }

    /**
     * Retrieves the untransformed position vector of the upper left boundary point
     *
     * @return untransformed position vector of upper left boundary point
     */
    public Vector2D getUpperLeftBoundary() {
        Vector2D normalisedOffset = offsetProperty.get().divide(scaleProperty.get());
        return new Vector2D(-normalisedOffset.getX(), normalisedOffset.getY());
    }

    /**
     * Retrieves the untransformed position vector of the lower right boundary point
     *
     * @return untransformed position vector of lower right boundary point
     */
    public Vector2D getLowerRightBoundary() {
        return getUpperLeftBoundary().add(boundarySize);
    }

    /**
     * Update boundary size according to the width and height of the {@link java.awt.Canvas}.
     *
     * @param width  width of the {@link java.awt.Canvas}
     * @param height height of the {@link java.awt.Canvas}
     */
    public void updateCanvasSize(double width, double height) {
        setBoundarySize(Vector2D.create(width, height).divide(scaleProperty.get()));
    }

    public void updateCanvasWidth(double width) {
        setBoundarySize(Vector2D.create(width / scaleProperty.get(), boundarySize.getY()));
    }

    public void updateCanvasHeight(double height) {
        setBoundarySize(Vector2D.create(boundarySize.getX(), height / scaleProperty.get()));
    }

    /**
     * Utility function to set lower right boundary vector according to boundary rectangle diagonal.
     *
     * @param width  width of boundary rectangle
     * @param height height of boundary rectangle
     */
    public void setBoundarySize(double width, double height) {
        setBoundarySize(new Vector2D(width, height));
    }

    /**
     * Set the offset based on the boundary representation.
     *
     * @param location upper left position vector of the boundary rectangle
     */
    private void setBoundaryLocation(Vector2D location) {
        setOffset(JTSUtils.negateX(location).multiply(scaleProperty.get()));
    }

    /**
     * Get the boundary rectangle width.
     *
     * @return boundary rectangle width
     */
    public double getBoundaryWidth() {
        return Math.abs(boundarySize.getX());
    }

    /**
     * Get the boundary rectangle height.
     *
     * @return boundary rectangle height
     */
    public double getBoundaryHeight() {
        return Math.abs(boundarySize.getY());
    }


    /**
     * Scales the canvas and keeps a certain point centered.
     *
     * @param gamma factor of new scale
     * @param point scaling source point
     */
    public void scaleRelative(double gamma, Vector2D point) {
        scaleOffset(gamma, point);
        double newScale = scaleProperty.get() * gamma;
        if (newScale > 0) {
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