package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.Line;
import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.EventBusUtils;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * This type of {@link Searcher} holds the numbers maxX, maxY, minX, minY, and updates these.
 * <p>
 * This {@link com.treasure.hunt.strategy.hider.Hider} works only for {@link com.treasure.hunt.strategy.hint.impl.AngleHint} with an angle &le; {@code {@link Math#PI}/2}.
 * Thus, this {@link Searcher} always gets one or two directions, in which the treasure lies for sure.
 * <p>
 * If the searcher knows only a minimum X, but no maximum X (or knows only a maximum X, but no minimum X),
 * he will go in the correct X-direction and double its moving-distance each time.
 * <p>
 * Otherwise, if the searcher knows both a minimum X and a maximum X,
 * he will go to the middle of these interval.
 * <p>
 * The same holds for the Y coordinates.
 *
 * @author dorianreineccius
 */
@Preference(name = PreferenceService.ANGLE_LOWER_BOUND, value = 0)
@Preference(name = PreferenceService.ANGLE_UPPER_BOUND, value = Math.PI / 2)
public class XYSearcher implements Searcher<AngleHint> {
    private Point searcherStartPosition;
    /**
     * These max/min X/Y values define halfplanes, in which the treasure lies for sure.
     */
    private double maxX, maxY, minX, minY;
    /**
     * maxXSet is true, if maxX were set yet. Otherwise it is true.
     * maxYSet, minXSet, minYSet is defined analogous.
     */
    private boolean maxXSet = false, maxYSet = false, minXSet = false, minYSet = false;
    /**
     * XSteps tells, that as long as the searcher knows only a minimum X, but no maximum X
     * (or knows only a maximum X, but no minimum X), he will go 2^XSteps into the correct X-direction.
     * The same holds for the Y-coordinate.
     */
    private int XSteps = 0, YSteps = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Point searcherStartPosition) {
        this.searcherStartPosition = searcherStartPosition;
    }

    /**
     * @return {@link SearchPath}, containing only the starting position.
     */
    @Override
    public SearchPath move() {
        return new SearchPath(this.searcherStartPosition);
    }

    /**
     * If the searcher knows only a minimum X, but no maximum X (or knows only a maximum X, but no minimum X),
     * he will go in the correct X-direction and double its moving-distance each time.
     * <p>
     * Otherwise, if the searcher knows both a minimum X and a maximum X,
     * he will go to the middle of these interval.
     * <p>
     * The same holds for the Y coordinates.
     */
    @Override
    public SearchPath move(AngleHint angleHint) {

        if (angleHint.getGeometryAngle().extend() > Math.PI / 2) {
            EventBusUtils.LOG_LABEL_EVENT.trigger(getClass().getSimpleName() + " only works for angles less or equal to 90 degrees.");
            return new SearchPath(this.searcherStartPosition);
        }

        Coordinate newCoordinate = searcherStartPosition.getCoordinate().copy();

        boolean up = false, right = false, down = false, left = false;
        int directions = 0;

        Coordinate leftWing = angleHint.getGeometryAngle().getLeft();
        Coordinate rightWing = angleHint.getGeometryAngle().getRight();

        /**
         * update minX,minY,maxX,maxY.
         */
        if (leftWing.y >= searcherStartPosition.getY() && rightWing.y >= searcherStartPosition.getY()) {
            up = true;
            directions++;
            if (minYSet) {
                minY = Math.max(minY, searcherStartPosition.getY());
            } else {
                minY = searcherStartPosition.getY();
            }
            minYSet = true;
        }
        if (leftWing.x >= searcherStartPosition.getX() && rightWing.x >= searcherStartPosition.getX()) {
            right = true;
            directions++;
            if (minXSet) {
                minX = Math.max(minX, searcherStartPosition.getX());
            } else {
                minX = searcherStartPosition.getX();
            }
            minXSet = true;
        }
        if (leftWing.y <= searcherStartPosition.getY() && rightWing.y <= searcherStartPosition.getY()) {
            down = true;
            directions++;
            if (maxYSet) {
                maxY = Math.min(maxY, searcherStartPosition.getY());
            } else {
                maxY = searcherStartPosition.getY();
            }
            maxYSet = true;
        }
        if (leftWing.x <= searcherStartPosition.getX() && rightWing.x <= searcherStartPosition.getX()) {
            left = true;
            directions++;
            if (maxXSet) {
                maxX = Math.min(maxX, searcherStartPosition.getX());
            } else {
                maxX = searcherStartPosition.getX();
            }
            maxXSet = true;
        }
        if (minXSet && maxXSet) {
            assert (minX <= maxX);
        }
        if (minYSet && maxYSet) {
            assert (minY <= maxY);
        }
        assert (1 <= directions && directions <= 2);

        /**
         * Move in the correct directions
         */
        if (up) {
            if (maxYSet && minYSet) {
                newCoordinate.y = (maxY + minY) / 2;
            } else {
                newCoordinate.y += Math.pow(2, YSteps++);
            }
        }
        if (right) {
            if (maxXSet && minXSet) {
                newCoordinate.x = (maxX + minX) / 2;
            } else {
                newCoordinate.x += Math.pow(2, XSteps++);
            }
        }
        if (down) {
            if (maxYSet && minYSet) {
                newCoordinate.y = (maxY + minY) / 2;
            } else {
                newCoordinate.y -= Math.pow(2, YSteps++);
            }
        }
        if (left) {
            if (maxXSet && minXSet) {
                newCoordinate.x = (maxX + minX) / 2;
            } else {
                newCoordinate.x -= Math.pow(2, XSteps++);
            }
        }

        Point newSearcherPosition = JTSUtils.createPoint(newCoordinate);
        SearchPath searchPath = new SearchPath(newSearcherPosition);
        /**
         * Completing additional items
         */
        if (maxXSet) {
            searchPath.addAdditionalItem(new GeometryItem<>(
                    new Line(new Coordinate(maxX, 0), new Coordinate(maxX, 1)),
                    GeometryType.MAX_X
            ));
        }
        if (maxYSet) {
            searchPath.addAdditionalItem(new GeometryItem<>(
                    new Line(new Coordinate(0, maxY), new Coordinate(1, maxY)),
                    GeometryType.MAX_Y
            ));
        }
        if (minXSet) {
            searchPath.addAdditionalItem(new GeometryItem<>(
                    new Line(new Coordinate(minX, 0), new Coordinate(minX, 1)),
                    GeometryType.MIN_X
            ));
        }
        if (minYSet) {
            searchPath.addAdditionalItem(new GeometryItem<>(
                    new Line(new Coordinate(0, minY), new Coordinate(1, minY)),
                    GeometryType.MIN_Y
            ));
        }
        searcherStartPosition = newSearcherPosition;
        return searchPath;
    }
}
