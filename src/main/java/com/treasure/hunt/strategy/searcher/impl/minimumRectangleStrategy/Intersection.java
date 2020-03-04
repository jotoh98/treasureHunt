package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import lombok.Value;
import org.locationtech.jts.geom.Coordinate;

@Value
public class Intersection {
    Coordinate coordinate;
    HalfPlaneHint hintOne;
    HalfPlaneHint hintTwo;
}
