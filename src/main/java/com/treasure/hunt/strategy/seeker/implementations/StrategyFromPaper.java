package com.treasure.hunt.strategy.seeker.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.tipster.AngleHint;
import com.treasure.hunt.strategy.seeker.Seeker;
import com.treasure.hunt.strategy.seeker.Moves;
import org.locationtech.jts.geom.Point;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class StrategyFromPaper implements Seeker<AngleHint> {
    public static GeometryType EXAMPLE_TYPE = new GeometryType(Color.BLUE, false, "example");

    @Override
    public void init() {
        //TODO: implement
    }

    @Override
    public void init(Point startPosition, GameHistory gameHistory) {

    }

    public Moves generate(AngleHint hint, Point currentLocation) {
        return null;
    }

    @Override
    public Moves getMoves(AngleHint hint, Point currentLocation) {
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
