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

/**
 * This type of {@link Searcher} just always passes the middle of a given {@link AngleHint},
 * by a distance of 1.
 *
 * @author dorianreineccius
 */
public class NaiveAngleSearcher implements Searcher<AngleHint> {
    private Point startPosition;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Point startPosition) {
        this.startPosition = startPosition;
    }

    /**
     * @return {@link Movement}, containing only the starting position.
     */
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
        Coordinate c1 = JTSUtils.middleOfAngleHint(angleHint.getAnglePointRight(), angleHint.getCenter(), angleHint.getAnglePointLeft());
        double x = c1.x;
        double y = c1.y;

        Movement m = new Movement(startPosition);
        startPosition = JTSUtils.createPoint(x, y);
        m.addWayPoint(startPosition);

        // Add to additionalItems
        Coordinate[] a2 = {angleHint.getGeometryAngle().getCenter(), new Coordinate(x, y)};
        m.addAdditionalItem(
                new GeometryItem(new LineString(
                        new CoordinateArraySequence(a2),
                        JTSUtils.GEOMETRY_FACTORY
                ), GeometryType.SEARCHER_MOVEMENT));
        return m;
    }
}
