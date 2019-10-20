package com.treasure.hunt.strategy.hint.hints;

import lombok.Value;
import org.locationtech.jts.geom.Point;

@Value
public class HintAndTarget<T extends Hint> {
    T hint;
    Point target;
}