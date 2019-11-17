package com.treasure.hunt.strategy.hint;

import lombok.Getter;
import lombok.Value;
import org.locationtech.jts.geom.Point;

public class AngleHint extends Hint {
    @Getter
    Point anglePointLeft;
    @Getter
    Point anglePointRight;

    public AngleHint(Point geometryItem, Point anglePointLeft, Point anglePointRight) {
        super(geometryItem);
        this.anglePointLeft = anglePointLeft;
        this.anglePointRight = anglePointRight;
    }
}
