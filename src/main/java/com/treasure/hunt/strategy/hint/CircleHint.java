package com.treasure.hunt.strategy.hint;

import lombok.Value;
import org.locationtech.jts.geom.Point;

@Value
public class CircleHint extends Hint {
    private double radius;

    public CircleHint(Point center, double radius) {
        super(center);
        this.radius = radius;
    }
}
