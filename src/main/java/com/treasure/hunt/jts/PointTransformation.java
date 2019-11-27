package com.treasure.hunt.jts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

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
        dest.setLocation(scale * src.x + offset.getX(), scale * -src.y + offset.getY());
    }

    public double diameter() {
        return Math.sqrt(boundary.width * boundary.width + boundary.height * boundary.height);
    }

    public void setBoundary(int x, int y, int width, int height) {
        boundary.setBounds(x, y, width, height);
    }
}
