package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.jts.geom.HalfPlane;
import com.treasure.hunt.jts.geom.Polyhedron;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PolyhedronSearcher implements Searcher<AngleHint> {

    Polyhedron polyhedron = new Polyhedron();

    List<Coordinate> movePositions = new ArrayList<>();
    Hull convHull = new Hull();

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

        convHull.addHalfPlane(new HalfPlane(hint.getGeometryAngle().getCenter(), hint.getGeometryAngle().getLeft()));
        convHull.addHalfPlane(new HalfPlane(hint.getGeometryAngle().getCenter(), hint.getGeometryAngle().getRight()));

        /*
        addAngle(hint.getGeometryAngle());
        polyhedron.resolve();
        */

        final Coordinate nextPosition;
        nextPosition = convHull.vertices.size() < 2 ? nextPosition(lastPosition) : JTSUtils.middleOfAngleHint(hint);
        movement.addWayPoint(JTSUtils.GEOMETRY_FACTORY.createPoint(nextPosition));
        movePositions.add(nextPosition);
        convHull.extend();
        return new Movement(lastPosition, nextPosition);
    }

    private Coordinate nextPosition(Coordinate lastPosition) {
        final Coordinate centroid = new ConvexHull((Geometry) convHull.vertices).getConvexHull().getCentroid().getCoordinate();
        /*final Coordinate centroid = polyhedron.getGeometry(true).convexHull().getCentroid().getCoordinate();*/
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

    class Hull {
        private List<Coordinate> vertices = new ArrayList<>();
        private Coordinate unbp0;
        private Coordinate unbp1;
        private LineSegment unbound;
        private HalfPlane plane;
        private List<LineSegment> convexHull = new ArrayList<>();

        public void addHalfPlane(HalfPlane half) {
            if (vertices.size() == 0) {
                if (plane == null) {
                    plane = half;
                    return;
                }
                final Coordinate intersect = plane.intersection(half);
                final Coordinate unbSec0 = plane.getDirection().normalize().translate(plane.p0);
                unbp0 = unbSec0;
                convexHull.add(new LineSegment(intersect, unbp0));
                final Coordinate unbSec1 = plane.getDirection().normalize().translate(plane.p1);
                unbp1 = unbSec1;
                convexHull.add(new LineSegment(unbp0, unbp1));
                convexHull.add(new LineSegment(unbp1, intersect));

            } else {
                LineSegment intersectionLine = new LineSegment();
                for (LineSegment line : convexHull) {
                    Coordinate inter = half.intersection(line);
                    if (inter != null) {
                        if (line.equalsTopo(new LineSegment(unbp0, unbp1))) {
                            changeLineSegment(line, inter, half);
                            unbp0 = line.p0;
                            unbp1 = line.p1;
                        }
                        if (intersectionLine.p0 == null) {
                            intersectionLine.p0 = inter;

                            break;
                        }
                        if (intersectionLine.p1 == null) {
                            intersectionLine.p1 = inter;
                            break;
                        }
                    }
                }
                convexHull.add(intersectionLine);
                convexHull = convexHull.stream()
                        .filter(line -> half.inside(line.p0) && half.inside(line.p1))
                        .collect(Collectors.toList());

            }
            vertices = convexHull.stream()
                    .map(line -> (new Coordinate[]{line.p0, line.p1}))
                    .flatMap(x -> Arrays.stream(x))
                    .distinct()
                    .collect(Collectors.toList());
        }

        private void changeLineSegment(LineSegment line, Coordinate inter, HalfPlane half) {
            if (half.inside(line.p1)) {
                line = new LineSegment(inter, line.p1);
            } else {
                line = new LineSegment(inter, line.p0);
            }
        }

        public void sortHull() {
            for (int i = 0; i < convexHull.size() - 1; i++) {
                Coordinate connect = convexHull.get(i).p1;
                for (int j = i + 1; j < convexHull.size(); j++) {
                    if (convexHull.get(j).p0.equals2D(connect)) {
                        Collections.swap(convexHull, i, j);
                        break;
                    }
                }
            }
        }

        public void extend() {
            sortHull();
            Coordinate newp0, newp1;

            LineSegment linebound = new LineSegment(unbp0, unbp1);
            List<LineSegment> unbounded = convexHull.stream()
                    .filter(line -> line.equalsTopo(linebound))
                    .collect(Collectors.toList());

            LineSegment h1 = convexHull.get(convexHull.indexOf(unbounded.get(0)) + 1 % convexHull.size());
            LineSegment h0 = convexHull.get(convexHull.indexOf(unbounded.get(0)) + 25 % convexHull.size());
            newp0 = (new Vector2D(h0.p0, h0.p1)).multiply(2).translate(h0.p1);
            h0.p1 = newp0;
            newp1 = (new Vector2D(h1.p1, h1.p0)).multiply(2).translate(h1.p0);
            h1.p0 = newp1;
            unbounded.get(0).setCoordinates(newp0, newp1);


        }

    }
}


