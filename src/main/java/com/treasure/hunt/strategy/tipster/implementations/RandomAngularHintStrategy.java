package com.treasure.hunt.strategy.tipster.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.tipster.Tipster;
import com.treasure.hunt.strategy.tipster.AngleHint;
import com.treasure.hunt.strategy.seeker.Moves;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomAngularHintStrategy implements Tipster<AngleHint> {
    public static GeometryType ANGLE_TYPE = new GeometryType(Color.BLUE, true, "Angle of hint");
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    @Override
    public void init(GameHistory gameHistory) {

    }

    public AngleHint generate(Moves moves) {
        Point currentLocationOfAgent = moves.getEndPoint().getObject();
        Point angleOne = new Point(null, new GeometryFactory()); //TODO: generate proper points
        Point angleTwo = new Point(null, new GeometryFactory()); //TODO: generate proper points
        AngleHint hint = new AngleHint(angleOne, angleTwo, currentLocationOfAgent);
        LineString lineString = new LineString(new CoordinateArraySequence(new Coordinate[]{angleOne.getCoordinate(), currentLocationOfAgent.getCoordinate()}), new GeometryFactory()); //TODO: this is probably wrong
        hint.addAdditionalItem(lineString, ANGLE_TYPE);
        return hint;
    }

    @Override
    public AngleHint generateHint(Moves moves) {
        return null;
    }

    @Override
    public List<GeometryType> getAvailableVisualisationGeometryTypes() {
        return Collections.singletonList(ANGLE_TYPE);
    }

    @Override
    public String getDisplayName() {
        return "Random Hint Generator smaller than pi angles";
    }
}
