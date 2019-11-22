package com.treasure.hunt.jts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.awt.geom.Point2D;

@AllArgsConstructor
@NoArgsConstructor
public class PointTransformation implements org.locationtech.jts.awt.PointTransformation {

    @Setter
    @Getter
    double scale = 1.0;

    @Setter
    @Getter
    Vector2D offset = new Vector2D(400, 400);

    @Override
    public void transform(Coordinate src, Point2D dest) {
        dest.setLocation(scale * src.x + offset.getX(), scale * -src.y + offset.getY());
    }
}
