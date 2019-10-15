package com.treasure.hunt.strategy.hint.hints;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.locationtech.jts.geom.Point;

@RequiredArgsConstructor
public class AngelByPointHint implements Hint<AngelByPointHint.AngelByPoints> {
    @Getter
    private final AngelByPoints hint;

    @Value
    public static class AngelByPoints {
        Point anglePointOne;
        Point anglePointTwo;
        Point angleCenter;
    }
}
