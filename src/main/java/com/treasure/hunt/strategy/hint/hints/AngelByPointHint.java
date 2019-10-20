package com.treasure.hunt.strategy.hint.hints;

import lombok.Value;
import org.locationtech.jts.geom.Point;

@Value
public class AngelByPointHint extends Hint {

    Point anglePointOne;
    Point anglePointTwo;
    Point angleCenter;
}
