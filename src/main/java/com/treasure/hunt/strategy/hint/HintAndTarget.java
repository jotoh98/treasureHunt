package com.treasure.hunt.strategy.hint;

import lombok.Value;
import org.locationtech.jts.geom.Point;

@Value
public class HintAndTarget<T extends Hint> extends Hint {
    T hint;
    Point target;
}