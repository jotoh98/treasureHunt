package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        convHull.addAngle(hint.getGeometryAngle().copy());

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

        private boolean isUnbound = true;

        private List<LineSegment> convexHull = new ArrayList<>();

        public void addAngle(final GeometryAngle angle) {
            if (vertices.size() == 0) {
                LineSegment unboundLineSegment = new LineSegment(angle.getLeft().copy(), angle.getRight().copy());

                convexHull.add(unboundLineSegment);
                pushUnbound(angle);

                unboundLineSegment = convexHull.get(0);
                convexHull.add(new LineSegment(angle.getCenter().copy(), unboundLineSegment.p0));
                convexHull.add(new LineSegment(unboundLineSegment.p1, angle.getCenter().copy()));

                vertices.add(unboundLineSegment.p0);
                vertices.add(unboundLineSegment.p1);
                vertices.add(angle.getCenter().copy());

                isUnbound = true;
            } else {
                addHalfPlane(new HalfPlane(angle.getCenter().copy(), angle.getLeft().copy()));
                addHalfPlane(new HalfPlane(angle.getRight().copy(), angle.getCenter().copy()));
            }

            vertices = convexHull.stream()
                    .flatMap(lineSegment -> Stream.of(lineSegment.p0, lineSegment.p1))
                    .distinct()
                    .collect(Collectors.toList());
        }

        public void addHalfPlane(HalfPlane halfPlane) {
            Coordinate intersectionP0 = null, intersectionP1 = null;

            for (int i = 0; i < convexHull.size(); i++) {
                final LineSegment lineSegment = convexHull.get(i);
                final Coordinate intersection = halfPlane.intersection(lineSegment);
                if (intersection != null) {

                    if (halfPlane.inside(lineSegment.p0)) {
                        intersectionP0 = intersection;
                    }
                    if (halfPlane.inside(lineSegment.p1)) {
                        intersectionP1 = intersection;
                    }
                    convexHull.set(i, cutLineSegment(lineSegment, halfPlane));

                    if (intersectionP0 != null && intersectionP1 != null) {
                        break;
                    }
                }
            }

            convexHull.add(new LineSegment(intersectionP0, intersectionP1));

            if (isUnbound && !halfPlane.inside(convexHull.get(0))) {
                isUnbound = false;
            }

            repairUnboundSegments();

            convexHull = convexHull.stream()
                    .filter(halfPlane::covers)
                    .collect(Collectors.toList());
        }

        public void repairUnboundSegments() {
            if (!isUnbound) {
                return;
            }
            LineSegment left = null;
            LineSegment right = null;
            LineSegment unboundSegment = convexHull.get(0);
            for (int i = 1; i < convexHull.size(); i++) {
                LineSegment lineSegment = convexHull.get(i);
                if (lineSegment.p1.distance(unboundSegment.p0) < 1e-10) {
                    left = lineSegment;
                }
                if (lineSegment.p0.distance(unboundSegment.p1) < 1e-10) {
                    right = lineSegment;
                }
            }
            pushUnbound(left, right);

        }

        public void pushUnbound(GeometryAngle angle) {
            pushUnbound(new LineSegment(angle.getCenter(), angle.getLeft()), new LineSegment(angle.getRight(), angle.getCenter()));
        }

        public void pushUnbound(LineSegment left, LineSegment right) {
            if (!isUnbound) {
                return;
            }

            final LineSegment unbound = convexHull.get(0);

            Vector2D transpose = Vector2D
                    .create(unbound.p0, unbound.p1)
                    .rotateByQuarterCircle(1)
                    .normalize()
                    .multiply(5);

            unbound.setCoordinates(
                    transpose.translate(unbound.p0),
                    transpose.translate(unbound.p1)
            );

            left.p1 = left.lineIntersection(unbound);
            right.p0 = right.lineIntersection(unbound);

            convexHull.get(0).p0 = left.p1.copy();
            convexHull.get(0).p1 = left.p0.copy();
        }

        private LineSegment cutLineSegment(LineSegment line, HalfPlane half) {
            final Coordinate inter = half.intersection(line);
            if (half.inside(line.p1)) {
                return new LineSegment(inter, line.p1);
            } else {
                return new LineSegment(line.p0, inter);
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

    }
}


