package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

public class NaiveAngleSearcher implements Searcher<AngleHint> {
    private static double DISTANCE = 1; // TODO
    private Point startPosition;

    @Override
    public void init(Point startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public Movement move() {
        return new Movement(startPosition);
    }

    /**
     * @param angleHint the hint, the {@link com.treasure.hunt.strategy.hider.Hider} gave last.
     * @return A {@link Movement} 1 length unit trough the middle of the AngleHint.
     */
    @Override
    public Movement move(AngleHint angleHint) {

        Coordinate c1 = JTSUtils.middleOfAngleHint(angleHint);
        double x = c1.x;
        double y = c1.y;

        //Testpurposes
        Coordinate[] a1 = {angleHint.getCenterPoint().getCoordinate(), angleHint.getAnglePointLeft().getCoordinate()};
        Coordinate[] a2 = {angleHint.getCenterPoint().getCoordinate(), new Coordinate(x, y)};
        Coordinate[] a3 = {angleHint.getCenterPoint().getCoordinate(), angleHint.getAnglePointRight().getCoordinate()};
        //end

        Movement m = new Movement(startPosition);
        startPosition = JTSUtils.createPoint(x, y);
        m.addWayPoint(startPosition);
        // Add to additionalItems
        Coordinate[] c = {m.getPoints().get(0).getObject().getCoordinate(),
                m.getEndPoint().getCoordinate()};
        /*m.addAdditionalItem(
                new GeometryItem(new LineString(
                        new CoordinateArraySequence(c),
                        JTSUtils.getDefaultGeometryFactory()
                ), GeometryType.SEARCHER_MOVEMENT));*/
        m.addAdditionalItem(
                new GeometryItem(new LineString(
                        new CoordinateArraySequence(a1),
                        JTSUtils.getDefaultGeometryFactory()
                ), GeometryType.SEARCHER_MOVEMENT));
        m.addAdditionalItem(
                new GeometryItem(new LineString(
                        new CoordinateArraySequence(a2),
                        JTSUtils.getDefaultGeometryFactory()
                ), GeometryType.SEARCHER_MOVEMENT));
        m.addAdditionalItem(
                new GeometryItem(new LineString(
                        new CoordinateArraySequence(a3),
                        JTSUtils.getDefaultGeometryFactory()
                ), GeometryType.SEARCHER_MOVEMENT));
        return m;
    }
}
