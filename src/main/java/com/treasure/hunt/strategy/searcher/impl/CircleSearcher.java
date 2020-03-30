package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

@Preference(name = PreferenceService.GLOBAL_GREEDY, value = 1)
public class CircleSearcher implements Searcher<CircleHint> {

    Point origin;

    @Override
    public void init(Point searcherStartPosition) {
        origin = searcherStartPosition;
    }

    @Override
    public SearchPath move() {
        return new SearchPath(origin);
    }

    @Override
    public SearchPath move(CircleHint hint) {
        Circle circle = hint.getCircle();

        Coordinate project = circle.project(origin.getCoordinate());

        if (project == null) {
            return new SearchPath(origin);
        }

        Point nextPosition = JTSUtils.createPoint(project);

        final SearchPath searchPath = new SearchPath(nextPosition);

        if (!isGlobalGreedy()) {
            origin = nextPosition;
        } else {
            searchPath.addAdditionalItem(new GeometryItem<>(JTSUtils.createLineString(origin, nextPosition), GeometryType.HELPER_LINE));
        }

        return searchPath;
    }

    private boolean isGlobalGreedy() {
        return PreferenceService.getInstance().getPreference(PreferenceService.GLOBAL_GREEDY, 1).intValue() == 1;
    }
}
