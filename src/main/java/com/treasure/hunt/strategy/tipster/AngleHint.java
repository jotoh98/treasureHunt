package com.treasure.hunt.strategy.tipster;

import lombok.Value;
import org.locationtech.jts.geom.Point;

@Value
public class AngleHint extends Hint {
    Point anglePointOne;
    Point anglePointTwo;
    Point angleCenter;
}
