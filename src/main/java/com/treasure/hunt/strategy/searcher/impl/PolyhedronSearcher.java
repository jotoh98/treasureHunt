package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.HalfPlane;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PolyhedronSearcher implements Searcher<AngleHint> {



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

        convHull.addHalfPlane(new HalfPlane(hint.getGeometryAngle().getCenter(), hint.getGeometryAngle().getLeft()), hint);
        convHull.addHalfPlane(new HalfPlane(hint.getGeometryAngle().getRight(), hint.getGeometryAngle().getCenter()), hint);

        final Coordinate nextPosition;
        nextPosition = convHull.vertices.size() < 2 ? JTSUtils.middleOfAngleHint(hint) : nextPosition(lastPosition);
        movement.addWayPoint(JTSUtils.GEOMETRY_FACTORY.createPoint(nextPosition));
        movement.addAdditionalItem(
                new GeometryItem<>(new ConvexHull(convHull.vertices.toArray(Coordinate[]::new),
                        JTSUtils.GEOMETRY_FACTORY).getConvexHull(),
                        GeometryType.HALFPLANE, new GeometryStyle(true, Color.BLUE)
                )
        );
        movePositions.add(nextPosition);
        movement.addWayPoint(JTSUtils.GEOMETRY_FACTORY.createPoint(nextPosition));
        //convHull.extend();
        return movement;
    }


    private Coordinate nextPosition(Coordinate lastPosition) {
        final Coordinate centroid = new ConvexHull(convHull.vertices.toArray(Coordinate[]::new), JTSUtils.GEOMETRY_FACTORY).getConvexHull().getCentroid().getCoordinate();
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

    class Hull {

        private List<Coordinate> vertices = new ArrayList<>();
        private Coordinate unbp0;
        private Coordinate unbp1;
        private HalfPlane plane;
        private List<LineSegment> convexHull = new ArrayList<>();

        public void addHalfPlane(HalfPlane halfray, AngleHint hint) {
            final HalfPlane half = halfray;

            if (vertices.size() == 0) {
                if (plane == null) {
                    plane = half;
                    return;
                }

                final Coordinate intersect = plane.intersection(half);

                Coordinate unbSec0 = plane.getDirection().normalize().multiply(10000).translate(plane.p0);
                unbSec0 = hint.getGeometryAngle().inView(unbSec0) ? unbSec0 : flipCoordinate(unbSec0);
                unbp0 = unbSec0;

                Coordinate unbSec1 = half.getDirection().normalize().multiply(10000).translate(half.p0);
                unbSec1 = hint.getGeometryAngle().inView(unbSec1) ? unbSec1 : flipCoordinate(unbSec1);
                unbp1 = unbSec1;

                vertices.add(unbp0);
                vertices.add(unbp1);
                vertices.add(intersect);

                convexHull.add(new LineSegment(intersect, unbp0));
                convexHull.add(new LineSegment(unbp0, unbp1));
                convexHull.add(new LineSegment(unbp1, intersect));


            } else {

                Coordinate intersectionLineX = null, intersectionLineY = null;
                for (int i = 0; i < convexHull.size(); i++) {
                    LineSegment line = convexHull.get(i);
                    Coordinate inter = half.intersection(line);
                    if (inter != null) {
                        convexHull.set(i, cutLineSegment(line, inter, half));

                        if (intersectionLineX == null) {
                            intersectionLineX = inter;
                            continue;
                        }
                        if (intersectionLineY == null) {
                            intersectionLineY = inter;
                            continue;
                        }
                    }
                }

                convexHull.add(new LineSegment(intersectionLineX, intersectionLineY));

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

        private LineSegment cutLineSegment(LineSegment line, Coordinate inter, HalfPlane half) {
            if (half.inside(line.p1)) {
                return new LineSegment(inter, line.p1);
            } else {
                return new LineSegment(inter, line.p0);
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

        private Coordinate flipCoordinate(Coordinate coordinate) {
            return new Coordinate(coordinate.x * (-1), coordinate.y * (-1));
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


