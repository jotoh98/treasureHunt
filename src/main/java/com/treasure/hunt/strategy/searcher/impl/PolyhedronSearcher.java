package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.jts.geom.HalfPlane;
import com.treasure.hunt.jts.geom.Ray;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
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


@Slf4j
public class PolyhedronSearcher implements Searcher<AngleHint> {


    List<Coordinate> movePositions = new ArrayList<>();
    private Hull convHull = new Hull();

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
        nextPosition = nextPosition(lastPosition);
        movement.addWayPoint(JTSUtils.GEOMETRY_FACTORY.createPoint(nextPosition));
        movement.addAdditionalItem(
                new GeometryItem<>(new ConvexHull(convHull.getVertices().toArray(Coordinate[]::new),
                        JTSUtils.GEOMETRY_FACTORY).getConvexHull(),
                        GeometryType.HALFPLANE, new GeometryStyle(true, Color.BLUE)
                )
        );

        for (int i = 0; i < convHull.convexHull.size(); i++) {
            movement.addAdditionalItem(
                    new GeometryItem<>(JTSUtils.createLineString(convHull.convexHull.get(i)),
                            GeometryType.HALFPLANE, new GeometryStyle(true, Color.RED)
                    )
            );
        }

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
        private Ray unboundLeft, unboundRight;
        private List<LineSegment> convexHull = new ArrayList<>();


        public void addHint(GeometryAngle newAngle) {
            final GeometryAngle angle = newAngle.copy();
            final Ray leftRay = newAngle.leftRay();
            final Ray rightRay = newAngle.rightRay();

            if (unboundLeft == null && unboundRight == null && convexHull.size() == 0) {
                unboundLeft = angle.leftRay();
                unboundRight = angle.rightRay();
                return;
            }

            //Check for intersections with left unbound Ray
            Coordinate leftInterLeft = leftRay.intersection(unboundLeft);
            Coordinate rightInterLeft = rightRay.intersection(unboundLeft);

            if (leftInterLeft != null && rightInterLeft != null) {
                if (unboundLeft.p0.distance(leftInterLeft) > unboundLeft.p0.distance(rightInterLeft)) {
                    convexHull.add(new LineSegment(unboundLeft.p0, leftInterLeft));
                    convexHull.add(new LineSegment(leftInterLeft, angle.getCenter()));
                    convexHull.add(new LineSegment(angle.getCenter(), rightInterLeft));

                    Coordinate newRayPoint = unboundLeft.getDirection().translate(rightInterLeft);
                    unboundLeft = new Ray(rightInterLeft, newRayPoint);

                } else {
                    convexHull.add(new LineSegment(leftInterLeft, rightInterLeft));
                    convexHull.add(new LineSegment(angle.getCenter(), leftInterLeft));
                    convexHull.add(new LineSegment(rightInterLeft, angle.getCenter()));
                    unboundLeft = null;
                    unboundRight = null;
                }
            }

            if (leftInterLeft != null) {
                Coordinate newRayPoint = unboundLeft.getDirection().translate(leftInterLeft);
                unboundLeft = new Ray(leftInterLeft, newRayPoint);
            }
            if (rightInterLeft != null) {
                convexHull.add(new LineSegment(unboundLeft.p0.copy(), rightInterLeft));
                unboundLeft = null;
                unboundRight = null;

            }

            Coordinate leftLineInter;
            Coordinate rightLineInter;

            List<Coordinate> intersectionsLeft = new ArrayList<>();
            List<Coordinate> intersectionsRight = new ArrayList<>();

            for (int i = 0; i < convexHull.size(); i++) {
                LineSegment line = convexHull.get(i);
                leftLineInter = leftRay.intersection(line);
                rightLineInter = rightRay.intersection(line);

                if (leftLineInter != null && rightLineInter != null) {
                    if (angle.inView(line.p0) && angle.inView(line.p1)) {
                        convexHull.add(new LineSegment(angle.getCenter(), leftInterLeft));
                        convexHull.add(new LineSegment(rightInterLeft, angle.getCenter()));
                        convexHull.remove(i);
                    }
                    convexHull.set(i, new LineSegment(leftLineInter, rightLineInter));
                    continue;
                }

                if (leftLineInter != null) {
                    intersectionsLeft.add(leftLineInter);
                    if (!angle.inView(line.p0)) {
                        line.p0 = leftLineInter;
                    }
                    if (!angle.inView(line.p1)) {
                        line.p1 = leftInterLeft;
                    }
                }

                if (rightLineInter != null) {
                    intersectionsRight.add(rightLineInter);
                    if (!angle.inView(line.p0)) {
                        line.p0 = leftLineInter;
                    }
                    if (!angle.inView(line.p1)) {
                        line.p1 = leftInterLeft;
                    }
                }
            }

            Coordinate leftInterRight = leftRay.intersection(unboundRight);
            Coordinate rightInterRight = rightRay.intersection(unboundRight);

            if (leftInterRight != null && rightInterRight != null) {
                if (unboundRight.p0.distance(leftInterRight) < unboundRight.p0.distance(rightInterRight)) {
                    convexHull.add(new LineSegment(unboundRight.p0, leftInterLeft));
                    convexHull.add(new LineSegment(leftInterLeft, angle.getCenter()));
                    convexHull.add(new LineSegment(angle.getCenter(), rightInterLeft));

                    Coordinate newRayPoint = unboundLeft.getDirection().translate(rightInterLeft);
                    unboundLeft = new Ray(rightInterLeft, newRayPoint);

                } else {
                    convexHull.add(new LineSegment(leftInterRight, rightInterRight));
                    convexHull.add(new LineSegment(angle.getCenter(), leftInterRight));
                    convexHull.add(new LineSegment(rightInterRight, angle.getCenter()));
                    unboundLeft = null;
                    unboundRight = null;
                }
            }

            if (rightInterRight != null) {
                Coordinate newRayPoint = unboundRight.getDirection().translate(leftInterRight);
                unboundRight = new Ray(leftInterRight, newRayPoint);
            }
            if (leftInterRight != null) {
                convexHull.add(new LineSegment(unboundRight.p0.copy(), rightInterRight));
                unboundLeft = null;
                unboundRight = null;

            }

            convexHull = convexHull.stream()
                    .filter(angle::inView)
                    .collect(Collectors.toList());

            vertices = convexHull.stream()
                    .flatMap(lineSegment -> Stream.of(lineSegment.p0, lineSegment.p1))
                    .distinct()
                    .collect(Collectors.toList());

            if (unboundLeft != null) {
                vertices.add(unboundLeft.getDirection().multiply(5).translate(unboundLeft.p1));
            }
            if (unboundRight != null) {
                vertices.add(unboundRight.getDirection().multiply(5).translate(unboundRight.p1));
            }
        }

        public void sortFromLeftToRight() {
            for (int i = 0; i < convexHull.size(); i++) {
                if (unboundLeft.p0.equals2D(convexHull.get(i).p0)) {
                    Collections.swap(convexHull, 1, i);
                    break;
                }
            }
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

        public void sortByDistance(List<Coordinate> sortMe, Coordinate distanceMe) {
            sortMe = sortMe.stream().sorted((a, b) -> closerTo(a, b, distanceMe)).collect(Collectors.toList());
        }

        public int closerTo(Coordinate a, Coordinate b, Coordinate c) {
            double aToC = c.distance(a);
            double bToC = c.distance(b);
            if (aToC < bToC) {
                if (aToC == bToC) {
                    return 0;
                }
                return 1;
            }
            return -1;
        }

        public void addLineSegsFromList(List<Coordinate> addMe, Coordinate center) {
            for (int i = 0; i < addMe.size() - 1; i++) {
                if (!areConnected(addMe.get(i), addMe.get(i + 1))) {
                    convexHull.add(new LineSegment(addMe.get(i), addMe.get(i + 1)));
                }
            }
            convexHull.add(new LineSegment(addMe.get(addMe.size() - 1), center));

        }

        public boolean areConnected(Coordinate a, Coordinate b) {

            int start = -1;
            int to = -1;
            for (int i = 0; i < convexHull.size(); i++) {
                if (convexHull.get(i).p0.equals2D(a)) {
                    start = i;
                }
                if (convexHull.get(i).p1.equals2D(b)) {
                    to = i;
                }
            }
            if (start < 0 || to < 0) {
                throw new IllegalStateException("Could not find Line Segment for arg Coordinate a or b");
            }
            for (int i = start; i < to; i++) {
                if (!convexHull.get(i).p1.equals2D(convexHull.get(i + 1).p0)) {
                    return false;
                }
            }
            return true;
        }

        public void addAngleHint(final GeometryAngle newAngle) {

            GeometryAngle angle = newAngle.copy();
            if (convexHull.size() == 0) {
                convexHull.add(new LineSegment(angle.getLeft(), angle.getRight()));
                convexHull.add(new LineSegment(angle.getCenter(), angle.getLeft()));
                convexHull.add(new LineSegment(angle.getRight(), angle.getCenter()));
                repairUnboundSegments();
                vertices = convexHull.stream()
                        .flatMap(lineSegment -> Stream.of(lineSegment.p0.copy(), lineSegment.p1.copy()))
                        .distinct()
                        .collect(Collectors.toList());

                return;
            }

            Coordinate leftIntersection = null;
            Coordinate rightIntersection = null;

            for (int i = 0; i < convexHull.size(); i++) {

                Coordinate leftInter = angle.leftRay().intersection(convexHull.get(i));
                Coordinate rightInter = angle.rightRay().intersection(convexHull.get(i));

                if (rightInter != null && leftInter != null) {
                    convexHull.set(i, new LineSegment(leftInter, rightInter));
                    leftIntersection = leftInter.copy();
                    rightIntersection = rightInter.copy();
                    break;
                }

                if (leftInter != null) {

                    leftIntersection = leftInter.copy();
                    if (!angle.inView(convexHull.get(i).p0)) {
                        convexHull.get(i).p0 = leftInter;

                    }
                    if (!angle.inView(convexHull.get(i).p1)) {
                        convexHull.get(i).p1 = leftInter;
                    }
                    throw new IllegalStateException("we have an intersection but both corners of the line segment is not in angle hints view");
                }

                if (rightInter != null) {
                    rightIntersection = rightInter.copy();
                    if (!angle.inView(convexHull.get(i).p0)) {
                        convexHull.get(i).p0 = rightInter;

                    }
                    if (!angle.inView(convexHull.get(i).p1)) {
                        convexHull.get(i).p1 = rightInter;

                    }
                    throw new IllegalStateException("we have an intersection but both corners of the line segment is not in angle hints view");
                }
            }

            if (leftIntersection == null || rightIntersection == null) {
                throw new IllegalStateException("There should be at least two intersections regarding one angle hint");
            }

            convexHull.add(new LineSegment(angle.getCenter().copy(), leftIntersection.copy()));
            convexHull.add(new LineSegment(rightIntersection.copy(), angle.getCenter().copy()));

            if (isUnbound && !angle.inView(convexHull.get(0))) {
                isUnbound = false;
            }

            repairUnboundSegments();

            convexHull = convexHull
                    .stream()
                    .filter(angle::inView)
                    .collect(Collectors.toList());

            vertices = convexHull.stream()
                    .flatMap(lineSegment -> Stream.of(lineSegment.p0, lineSegment.p1))
                    .distinct()
                    .collect(Collectors.toList());


        }

        public void cutLineSegements(GeometryAngle angle) {
            for (int i = 0; i < convexHull.size(); i++) {

                Coordinate leftIntersection = angle.leftRay().intersection(convexHull.get(i));
                Coordinate rightIntersection = angle.rightRay().intersection(convexHull.get(i));

                if (rightIntersection != null && leftIntersection != null) {
                    convexHull.set(i, new LineSegment(leftIntersection, rightIntersection));

                    return;
                }

                if (leftIntersection != null) {
                    if (!angle.inView(convexHull.get(i).p0)) {
                        convexHull.get(i).p0 = leftIntersection;
                        break;
                    }
                    if (!angle.inView(convexHull.get(i).p1)) {
                        convexHull.get(i).p1 = leftIntersection;
                        break;
                    }
                    throw new IllegalStateException("we have an intersection but both corners of the line segment is not in angle hints view");
                }

                if (rightIntersection != null) {
                    if (!angle.inView(convexHull.get(i).p0)) {
                        convexHull.get(i).p0 = rightIntersection;
                        break;
                    }
                    if (!angle.inView(convexHull.get(i).p1)) {
                        convexHull.get(i).p1 = rightIntersection;
                        break;
                    }
                    throw new IllegalStateException("we have an intersection but both corners of the line segment is not in angle hints view");
                }
            }
        }

        public void addAngle(final GeometryAngle angle) {
            if (vertices.size() == 0) {
                LineSegment unboundLineSegment = new LineSegment(angle.getLeft(), angle.getRight());

                final double distance = unboundLineSegment.distance(angle.getCenter());
                if (distance < 1d) {
                    unboundLineSegment.p0.setCoordinate(
                            angle.leftVector().normalize().divide(distance * 0.5).translate(angle.getCenter())
                    );
                    unboundLineSegment.p1.setCoordinate(
                            angle.rightVector().normalize().divide(distance * 0.5).translate(angle.getCenter())
                    );
                }

                convexHull.add(unboundLineSegment);
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
                assert (lineSegment != null);
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

            if (intersectionP0 == null || intersectionP1 == null) {
                throw new IllegalStateException("HalfPlane in convex polygon must return two intersections.");
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

            if (convexHull.get(0).distance(left.p0) < 10) {
                pushUnbound(left, right);
            }

        }

        public void pushUnbound(GeometryAngle angle) {
            pushUnbound(new LineSegment(angle.getCenter().copy(), angle.getLeft().copy()), new LineSegment(angle.getRight().copy(), angle.getCenter().copy()));
        }

        public void pushUnbound(LineSegment left, LineSegment right) {
            if (!isUnbound) {
                return;
            }
            Coordinate unbound0 = Vector2D.create(left.p0, left.p1).multiply(5).translate(left.p0);
            Coordinate unbound1 = Vector2D.create(right.p1, right.p0).multiply(5).translate(right.p1);

            if (unbound0 == null || null == unbound1) {
                throw new IllegalStateException("translated point may not be null");
            }

            convexHull.get(0).setCoordinates(unbound0, unbound1);
            left.p1 = unbound0;
            right.p0 = unbound1;
        }

        private LineSegment cutLineSegment(LineSegment line, HalfPlane half) {
            final Coordinate inter = half.intersection(line);
            if (half.inside(line.p1)) {
                return new LineSegment(inter, line.p1);
            } else {
                return new LineSegment(line.p0, inter);
            }
        }

        public List<Coordinate> getVertices() {
            return vertices
                    .stream()
                    .map(Coordinate::copy)
                    .collect(Collectors.toList());
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


