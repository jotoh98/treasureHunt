package com.treasure.hunt.jts;

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
    double scale = 1.0;
    /**
     * The offset translates the source coordinates additive in {@link PointTransformation#transform(Coordinate)}.
     */
    @Getter
    @Setter
    Vector2D offset = new Vector2D(400, 400);

    @Setter
    @Getter
    private Vector2D boundarySize = new Vector2D(0, 0);

    public void setScale(double scale) {
        if (scale != 0) {
            this.scale = scale;
        }
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
     * Transform method between {@link Coordinate}s.
     *
     * @param src the coordinate we want to transform
     * @return transformed coordinate
     */
    public Coordinate transform(Coordinate src) {
        return new Coordinate(transformX(src.x), transformY(src.y));
    }

    public double transformX(double x) {
        return scale * x + offset.getX();
    }

    public double transformY(double y) {
        return scale * -y + offset.getY();
    }

}