package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.jts.geom.HalfPlane;
import com.treasure.hunt.jts.geom.Polyhedron;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class PolyhedronSearcher implements Searcher<AngleHint> {

    Polyhedron polyhedron = new Polyhedron();

    List<Coordinate> movePositions = new ArrayList<>();

    @Override
    public void init(Point searcherStartPosition) {
        movePositions.add(searcherStartPosition.getCoordinate());
    }

    @Override
    public Movement move() {
        return new Movement(JTSUtils.GEOMETRY_FACTORY.createPoint(movePositions.get(0)));
    }

    @Override
    public Movement move(AngleHint hint) {
        final Coordinate lastPosition = lastMove();
        final Movement movement = new Movement(JTSUtils.GEOMETRY_FACTORY.createPoint(lastPosition));

        addAngle(hint.getGeometryAngle());
        polyhedron.resolve();
        final Coordinate nextPosition = nextPosition(lastPosition);
        movePositions.add(nextPosition);
        movement.addWayPoint(JTSUtils.GEOMETRY_FACTORY.createPoint(nextPosition));
        return new Movement(lastPosition, nextPosition);
    }

    private Coordinate nextPosition(Coordinate lastPosition) {
        final Coordinate centroid = polyhedron.getGeometry(false).convexHull().getCentroid().getCoordinate();
        return JTSUtils.coordinateInDistance(lastPosition, centroid, 1);
    }

    /**
     * Get the last move coordinate.
     *
     * @return coordinate of last move
     */
    private Coordinate lastMove() {
        return movePositions.get(movePositions.size() - 1);
    }

    /**
     * Add a {@link GeometryAngle} to the {@link Polyhedron}.
     * The two angle sides are converted to half planes which are added
     * to the polayhedron.
     *
     * @param angle the angle which is added
     */
    private void addAngle(GeometryAngle angle) {
        polyhedron.addHalfPlane(new HalfPlane(angle.getCenter(), angle.getLeft()));
        polyhedron.addHalfPlane(new HalfPlane(angle.getCenter(), angle.getRight()));
    }

}
