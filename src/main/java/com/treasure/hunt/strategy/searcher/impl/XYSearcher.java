package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.HalfPlane;
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
 * This TODO
 * <p>
 * This {@link com.treasure.hunt.strategy.hider.Hider} works only for {@link com.treasure.hunt.strategy.hint.impl.AngleHint} with an angle <= {@code {@link Math#PI}/2}.
 *
 * @author dorianreineccius
 */
@Preference(name = PreferenceService.ANGLE_UPPER_BOUND, value = Math.PI / 2)
public class XYSearcher implements Searcher<AngleHint> {
    private Point searcherStartPosition;
    private double maxX, maxY, minX, minY;
    /**
     * XInterval is true, if maxX and minX are set.
     * YInterval is defined analogous.
     */
    private boolean XInterval = false, YInterval = false;
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

    @Override
    public SearchPath move(AngleHint angleHint) {

        if (angleHint.getGeometryAngle().extend() > Math.PI / 2) {
            EventBusUtils.LOG_LABEL_EVENT.trigger(getClass().getSimpleName() + " only works for angles less or equal to 90 degrees.");
            return new SearchPath(this.searcherStartPosition);
        }

        Coordinate newCoordinate = searcherStartPosition.getCoordinate().copy();

        boolean up = false, right = false, down = false, left = false;
        int directions = 0;

        Coordinate leftWing = ((AngleHint) angleHint).getGeometryAngle().getLeft();
        Coordinate rightWing = ((AngleHint) angleHint).getGeometryAngle().getRight();
        if (leftWing.y >= searcherStartPosition.getY() && rightWing.y >= searcherStartPosition.getY()) {
            minY = Math.max(minY, searcherStartPosition.getY());
            up = true;
            directions++;
        }
        if (leftWing.x >= searcherStartPosition.getX() && rightWing.x >= searcherStartPosition.getX()) {
            minX = Math.max(minX, searcherStartPosition.getX());
            right = true;
            directions++;
        }
        if (leftWing.y <= searcherStartPosition.getY() && rightWing.y <= searcherStartPosition.getY()) {
            maxY = Math.min(maxY, searcherStartPosition.getY());
            down = true;
            directions++;
        }
        if (leftWing.x <= searcherStartPosition.getX() && rightWing.x <= searcherStartPosition.getX()) {
            maxX = Math.min(maxX, searcherStartPosition.getX());
            left = true;
            directions++;
        }
        assert (1 <= directions && directions <= 2);

        if (up) {
            if (YInterval) {
                newCoordinate.y = (maxY + minY) / 2;
            } else {
                newCoordinate.y += Math.pow(2, YSteps++);
            }
        }
        if (right) {
            if (XInterval) {
                newCoordinate.x = (maxX + minX) / 2;
            } else {
                newCoordinate.x += Math.pow(2, XSteps++);
            }
        }
        if (down) {
            if (YInterval) {
                newCoordinate.y = (maxY + minY) / 2;
            } else {
                newCoordinate.y -= Math.pow(2, YSteps++);
            }
        }
        if (left) {
            if (XInterval) {
                newCoordinate.x = (maxX + minX) / 2;
            } else {
                newCoordinate.x -= Math.pow(2, XSteps++);
            }
        }
        Point newSearcherPosition = JTSUtils.createPoint(newCoordinate);
        SearchPath searchPath = new SearchPath(newSearcherPosition);
        if (up) {
            searchPath.addAdditionalItem(new GeometryItem<>(
                    new HalfPlane(searcherStartPosition.getCoordinate(), new Coordinate(searcherStartPosition.getCoordinate().x + 1, searcherStartPosition.getCoordinate().y)),
                    GeometryType.HALF_PLANE
            ));
        }
        if (right) {
            searchPath.addAdditionalItem(new GeometryItem<>(
                    new HalfPlane(searcherStartPosition.getCoordinate(), new Coordinate(searcherStartPosition.getCoordinate().x, searcherStartPosition.getCoordinate().y - 1)),
                    GeometryType.HALF_PLANE
            ));
        }
        if (down) {
            searchPath.addAdditionalItem(new GeometryItem<>(
                    new HalfPlane(searcherStartPosition.getCoordinate(), new Coordinate(searcherStartPosition.getCoordinate().x - 1, searcherStartPosition.getCoordinate().y)),
                    GeometryType.HALF_PLANE
            ));
        }
        if (left) {
            searchPath.addAdditionalItem(new GeometryItem<>(
                    new HalfPlane(searcherStartPosition.getCoordinate(), new Coordinate(searcherStartPosition.getCoordinate().x, searcherStartPosition.getCoordinate().y + 1)),
                    GeometryType.HALF_PLANE
            ));
        }
        searcherStartPosition = newSearcherPosition;
        return searchPath;
    }
}
