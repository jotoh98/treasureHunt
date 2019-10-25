package com.treasure.hunt.strategy.moves.implemenations;

import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.moves.AbstractMovesGenerator;
import com.treasure.hunt.strategy.moves.Moves;
import org.locationtech.jts.geom.Point;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class StrategyFromPaper extends AbstractMovesGenerator<AngleHint> {
    public static GeometryType EXAMPLE_TYPE = new GeometryType(Color.BLUE, false, "example");

    @Override
    public void init() {
        //TODO: implement
    }

    protected Moves generate(AngleHint hint, Point currentLocation) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Strategy from Paper";
    }


    @Override
    public List<GeometryType> getAvailableVisualisationGeometryTypes() {
        List<GeometryType> availableVisualisationGeometryTypes = super.getAvailableVisualisationGeometryTypes();
        List<GeometryType> geometryTypes = Arrays.asList(EXAMPLE_TYPE);
        geometryTypes.addAll(availableVisualisationGeometryTypes);
        return geometryTypes;
    }
}
