package com.treasure.hunt.strategy.hint.implementations;

import com.treasure.hunt.strategy.hint.AbstractHintGenerator;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.hint.HintAndTarget;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryItem;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.awt.*;
import java.util.Random;

public class RandomAngularHintStrategy extends AbstractHintGenerator<AngleHint> {
    private static final VisualisationGeometryType ANGLE_VISUALISATION = new VisualisationGeometryType("Angle of hint", Color.BLUE, true);
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    @Override
    protected HintAndTarget<AngleHint> generate(Point currentLocationOfAgent) {
        Point angleOne = new Point(null, new GeometryFactory()); //TODO: generate proper points
        Point angleTwo = new Point(null, new GeometryFactory()); //TODO: generate proper points
        AngleHint hint = new AngleHint(angleOne, angleTwo, currentLocationOfAgent);
        LineString lineString = new LineString(new CoordinateArraySequence(new Coordinate[]{angleOne.getCoordinate(), currentLocationOfAgent.getCoordinate()}), new GeometryFactory()); //TODO: this is probably wrong
        VisualisationGeometryItem visualisationGeometryItem = new VisualisationGeometryItem(lineString, ANGLE_VISUALISATION);
        getVisualisationGeometryList().add(visualisationGeometryItem);
        return new HintAndTarget<>(hint, new Point(null, new GeometryFactory()));
    }

    @Override
    public String getDisplayName() {
        return "Random Hint Generator smaller than pi angles";
    }
}
