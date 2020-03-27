package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.Polyhedron;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.EventBusUtils;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PolyhedronSearcher implements Searcher<AngleHint> {


    List<Point> movePositions = new ArrayList<>();

    Polyhedron polyhedron = new Polyhedron();

    @Override
    public void init(Point searcherStartPosition) {
        movePositions.add(searcherStartPosition);
    }

    @Override
    public SearchPath move() {
        return new SearchPath(movePositions.get(0));
    }

    @Override
    public SearchPath move(AngleHint hint) {

        if (hint.getGeometryAngle().extend() > Math.PI) {
            EventBusUtils.LOG_LABEL_EVENT.trigger(getClass().getSimpleName() + " only works for angles less than 180 degrees.");
            return new SearchPath(lastMove());
        }

        polyhedron.addAngle(hint.getGeometryAngle());

        final Point nextPosition = nextPosition(hint);

        movePositions.add(nextPosition);

        final SearchPath searchPath = new SearchPath(nextPosition);
        searchPath.addAdditionalItem(
                new GeometryItem<>(
                        polyhedron.getConvexHull().copy(),
                        GeometryType.POLYHEDRON,
                        new GeometryStyle(true, Color.BLUE, new Color(0, 0, 255, 64))
                )
        );

        return searchPath;
    }


    private Point nextPosition(AngleHint hint) {
        if (polyhedron.getConvexHull().getCoordinates().length < 3) {
            return JTSUtils.GEOMETRY_FACTORY.createPoint(
                    JTSUtils.middleOfAngleHint(hint)
            );
        }

        final Coordinate centroid = polyhedron.getConvexHull().getCentroid().getCoordinate();
        return JTSUtils.GEOMETRY_FACTORY.createPoint(
                JTSUtils.coordinateInDistance(lastMove().getCoordinate(), centroid, 1)
        );
    }

    /**
     * Get the last move coordinate.
     *
     * @return coordinate of last move
     */
    private Point lastMove() {
        return movePositions.get(movePositions.size() - 1);
    }
}


