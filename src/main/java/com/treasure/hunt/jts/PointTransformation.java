package com.treasure.hunt.jts;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.awt.geom.Point2D;

/**
 * @author hassel
 */
@AllArgsConstructor
@NoArgsConstructor
public class PointTransformation implements org.locationtech.jts.awt.PointTransformation {

    @Setter
    double scale = 1.0;

    @Setter
    Vector2D offset = new Vector2D();

    @Override
    public void transform(Coordinate src, Point2D dest) {
        dest.setLocation(400 + scale * src.x + offset.getX(), 400 + scale * -src.y + offset.getY());
    }
}
