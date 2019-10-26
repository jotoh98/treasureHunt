package com.treasure.hunt.strategy.tipster.implementations;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.tipster.Tipster;
import com.treasure.hunt.strategy.hint.AngleHint;
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
    private static final Random RANDOM = new Random(System.currentTimeMillis()); // truly random ;)

    private GeometryFactory gf = new GeometryFactory();

    protected AngleHint generate(Moves moves) {
        Point currentLocationOfSeeker = moves.getEndPoint().getObject();
        Point angleOne = gf.createPoint(new Coordinate(0, 0)); //TODO: generate proper points
        Point angleTwo = gf.createPoint(new Coordinate(0, 0)); //TODO: generate proper points
        AngleHint hint = new AngleHint(angleOne, angleTwo, currentLocationOfSeeker);
        LineString lineString = new LineString(new CoordinateArraySequence(new Coordinate[]{angleOne.getCoordinate(), currentLocationOfSeeker.getCoordinate()}), new GeometryFactory()); //TODO: this is probably wrong
        hint.addAdditionalItem(lineString, ANGLE_TYPE);
        return hint;
    }

    @Override
    public void init(double insecurity) {

    }

    @Override
    public AngleHint generateHint() {
        return null;
    }

    @Override
    public AngleHint generateHint(Moves moves) {
        return null;
    }

    @Override
    public List<GeometryItem> getAvailableVisualisationGeometryItems() {
        Point treasurePos = gf.createPoint(new Coordinate(0, 0));
        GeometryItem<Point> treasurePosItem = new GeometryItem<Point>(treasurePos, GeometryType.TREASURE_LOCATION);
        return Collections.singletonList(treasurePosItem);
    }

    @Override
    public String getDisplayName() {
        return "Random Hint Generator smaller than pi angles";
    }

    @Override
    public Point getTreasureLocation() {
        return gf.createPoint(new Coordinate(0, 0));
    }
}
