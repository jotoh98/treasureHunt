package com.treasure.hunt.jts;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.awt.geom.Point2D;

/**
 * @author hassel
 */
public class PointTransformation implements org.locationtech.jts.awt.PointTransformation {
    double scale = 1.0;
    Vector2D offset = new Vector2D();

    public PointTransformation(double scale, Vector2D offset) {
        this.scale = scale;
        this.offset = offset;
    }

    public PointTransformation() {
    }

    @Override
    public void transform(Coordinate src, Point2D dest) {
        dest.setLocation(400 + scale * src.x + offset.getX(), 400 + scale * -src.y + offset.getY());
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setOffset(Vector2D offset) {
        this.offset = offset;
    }
}
