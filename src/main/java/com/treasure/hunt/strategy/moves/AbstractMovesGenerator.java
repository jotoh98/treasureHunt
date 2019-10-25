package com.treasure.hunt.strategy.moves;

import com.treasure.hunt.strategy.Generator;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import org.locationtech.jts.geom.Point;

import java.awt.*;
import java.util.Collections;
import java.util.List;


public abstract class AbstractMovesGenerator<T extends Hint> extends Generator {
    public static GeometryType WAY_POINT = new GeometryType(Color.BLACK, true, "Way points");

    public abstract void init();

    protected abstract Moves generate(T moves, Point currentLocation);

    public Moves getMoves(T hint, Point currentLocation) {
        return generate(hint, currentLocation);
    }

    public List<GeometryType> getAvailableVisualisationGeometryTypes() {
        return Collections.singletonList(WAY_POINT);
    }
}
