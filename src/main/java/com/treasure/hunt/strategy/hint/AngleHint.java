package com.treasure.hunt.strategy.hint;

import lombok.ToString;
import lombok.Value;
import org.locationtech.jts.geom.Point;

@Value
@ToString(of = {"anglePointLeft", "anglePointRight", "center"})
public class AngleHint extends Hint {
    Point anglePointLeft;
    Point anglePointRight;

    public AngleHint(Point geometryItem, Point anglePointLeft, Point anglePointRight) {
        super(geometryItem);
        this.anglePointLeft = anglePointLeft;
        this.anglePointRight = anglePointRight;
    }
}
