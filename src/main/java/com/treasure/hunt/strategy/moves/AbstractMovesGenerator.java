package com.treasure.hunt.strategy.moves;

import com.treasure.hunt.strategy.Generator;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryItem;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryType;
import org.locationtech.jts.geom.Point;

import java.awt.*;


public abstract class AbstractMovesGenerator<H extends Hint> extends Generator {
    private static VisualisationGeometryType WAY_POINT = new VisualisationGeometryType("Way points", Color.BLACK, true);

    public abstract void init();

    protected abstract Moves generate(H hint, Point currentLocation);

    public Moves getMoves(H hint, Point currentLocation) {
        VisualisationGeometryItem wayPoint = new VisualisationGeometryItem(currentLocation, WAY_POINT);
        visualisationGeometryList.add(wayPoint);
        return generate(hint, currentLocation);
    }
}
