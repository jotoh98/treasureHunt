package com.treasure.hunt.strategy.seeker.implemenations;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.seeker.Seeker;
import com.treasure.hunt.strategy.seeker.Moves;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.Arrays;
import java.util.List;

public class StrategyFromPaper implements Seeker<AngleHint> {
    private GeometryFactory gf = new GeometryFactory();
    GeometryItem exampleGeometryItem = new GeometryItem(gf.createPoint(new Coordinate(0, 0)),
            GeometryType.WAY_POINT);

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
    public void init(Point position) {

    }

    @Override
    public Moves generate() {
        return null;
    }

    @Override
    public Moves generate(AngleHint moves) {
        return null;
    }

    @Override
    public List<GeometryItem> getAvailableVisualisationGeometryItems() {
        List<GeometryItem> availableVisualisationGeometryTypes = Arrays.asList(exampleGeometryItem);
        return availableVisualisationGeometryTypes;
    }
}
