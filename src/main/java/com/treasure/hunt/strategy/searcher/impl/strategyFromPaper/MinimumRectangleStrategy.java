package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Value;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.geom.util.NoninvertibleTransformationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MinimumRectangleStrategy implements Searcher<HalfPlaneHint> {
    Point searcherStartPosition;
    private StrategyFromPaper strategyFromPaper;
    private boolean firstMoveWithHint = true;
    private int phase;
    private AffineTransformation fromPaper;
    private AffineTransformation forPaper;
    private List<HalfPlaneHint> oldObtainedHints;// received before the last update of the phase's rectangle //TODO init
    private List<HalfPlaneHint> newObtainedHints;// received after the last update of the phase's rectangle //TODO init
    /**
     * This points represent the polygon where the treasure must lie in if it is in the current search rectangle,
     * according to all obtained hints.
     * If this List is empty, the treasure is not in the current search rectangle.
     */
    private List<Intersection> polygonPoints; // TODO init
    /**
     * This are not real obtained hints.
     * This hints are just the borders of the current phase rectangle interpreted as hints.
     * All hintslines go from one corner of the current phase rectangle to another and show in a direction that the
     * hole phase rectangle lies in the treasure area.
     */
    private ArrayList<HalfPlaneHint> phaseHints;

    /**
     * @param searcherStartPosition the {@link Searcher} starting position,
     *                              he will initialized on.
     */
    @Override
    public void init(Point searcherStartPosition) {
        strategyFromPaper = new StrategyFromPaper();
        strategyFromPaper.init(JTSUtils.createPoint(0, 0));
        this.searcherStartPosition = searcherStartPosition;
        phase = 1;
        phaseHints = new ArrayList<>(4);
        oldObtainedHints = new ArrayList<>();
        newObtainedHints = new ArrayList<>();
    }

    /**
     * Use this to perform a initial move, without a hint given.
     * This is for the case, the searcher starts. (as he does normally)
     *
     * @return {@link Movement} the {@link Movement} the searcher did
     */
    @Override
    public Movement move() {
        return strategyFromPaper.move();
    }

    /**
     * @param hint the hint, the {@link Hider} gave last.
     * @return {@link Movement} the {@link Movement}, this searcher chose.
     */
    @Override
    public Movement move(HalfPlaneHint hint) {
        newObtainedHints.add(hint);
        if (firstMoveWithHint) {
            firstMoveWithHint = false;

            double sinHintAngle = hint.getRight().y - hint.getCenter().y;
            double cosHintAngle = hint.getRight().x - hint.getCenter().x;
            fromPaper = AffineTransformation.rotationInstance(sinHintAngle, cosHintAngle);
            try {
                forPaper = fromPaper.getInverse();
            } catch (NoninvertibleTransformationException e) {
                throw new RuntimeException("Matrix was not invertible " + Arrays.toString(fromPaper.getMatrixEntries()),
                        e);
            }
            return transformFromPaper(strategyFromPaper.move(
                    new HalfPlaneHint(new Coordinate(0, 0), new Coordinate(1, 0))
            )); // the initial input hint for the strategy from the paper by definition shows upwards (in this strategy)
        }

        if (strategyFromPaper.rectangleNotLargeEnough()) {
            Movement move = new Movement();
            scanCurrentRectangle(move);
            ArrayList<HalfPlaneHint> oldPhaseHints = phaseHints;
            phase++;
            strategyFromPaper.phase++;
            updatePhaseHints();
            updatePolygonPoints(oldPhaseHints);
            setABCDinStrategy();
            addPolygonVisualization(move);
            GeometricUtils.moveToCenterOfRectangle(strategyFromPaper.searchAreaCornerA,
                    strategyFromPaper.searchAreaCornerB, strategyFromPaper.searchAreaCornerC,
                    strategyFromPaper.searchAreaCornerD, move);
            oldObtainedHints.addAll(newObtainedHints);
            newObtainedHints.clear();
            return move;
        } else
            return transformFromPaper(strategyFromPaper.move(transformForPaper(hint)));
    }

    /**
     * Tests if the intersection of hintOne and hintTwo lies in all other half-planes of the hints in otherHints.
     * If this is the case the intersection is returned, otherwise null is returned.
     *
     * @param hintOne
     * @param hintTwo
     * @param otherHints
     * @return
     */
    private Intersection intersectionInPolygon(HalfPlaneHint hintOne, HalfPlaneHint hintTwo,
                                               List<HalfPlaneHint> otherHints) {
        Coordinate intersection = hintOne.getHalfPlaneLine().intersection(hintTwo.getHalfPlaneLine());
        if (intersection == null)
            return null;

        for (HalfPlaneHint hint : otherHints) {
            if (hint != hintOne && hint != hintTwo) {
                // determine whether intersection is in hint or not if it isen't, return null otherwise do nothing
            }
        }
        return new Intersection(intersection, hintOne, hintTwo);
    }

    /**
     * Tests if the intersection of hintOne and hintTwo lies in all other half-planes of the hints in otherHintsOne,
     * otherHintsTwo and otherHintsThree.
     * If this is the case the intersection gets added to intersectionList, otherwise nothing is done.
     *
     * @param hintOne
     * @param hintTwo
     * @param otherHintsOne
     * @param otherHintsTwo
     * @param otherHintsThree
     */
    private void addIntersectionIfInPoly(HalfPlaneHint hintOne, HalfPlaneHint hintTwo,
                                         List<HalfPlaneHint> otherHintsOne, List<HalfPlaneHint> otherHintsTwo,
                                         List<HalfPlaneHint> otherHintsThree,
                                         List<Intersection> intersectionList) {
        Coordinate intersection = hintOne.getHalfPlaneLine().intersection(hintTwo.getHalfPlaneLine());
        if (intersection == null)
            return;

        for (HalfPlaneHint hint : otherHintsOne) {
            if (hint != hintOne && hint != hintTwo) {
                if (!hint.inHalfPlane(intersection))
                    return;
            }
        }

        for (HalfPlaneHint hint : otherHintsTwo) {
            if (hint != hintOne && hint != hintTwo) {
                if (!hint.inHalfPlane(intersection))
                    return;
            }
        }
        for (HalfPlaneHint hint : otherHintsThree) {
            if (hint != hintOne && hint != hintTwo) {
                if (!hint.inHalfPlane(intersection))
                    return;
            }
        }
        intersectionList.add(new Intersection(intersection, hintOne, hintTwo));
    }

    /**
     * When the phase has changed, this method can be called and the polygon points are set accordingly.
     *
     * @param oldPhaseHints The phase hints of the previous phase
     */
    private void updatePolygonPoints(ArrayList<HalfPlaneHint> oldPhaseHints) {
        List<Intersection> newPolygonPoints = new LinkedList<>();

        // iterate over the old intersections, and look if they can be reused:
        for (Intersection intersection : polygonPoints) {
            if (!(oldPhaseHints.contains(intersection.getHintOne()) ||
                    oldPhaseHints.contains(intersection.getHintTwo()))) {
                for (HalfPlaneHint testHintHP : newObtainedHints) {
                    if (testHintHP.inHalfPlane(intersection.getCoordinate())) {
                        newPolygonPoints.add(intersection);
                    }
                }
                for (HalfPlaneHint testHintHP : oldPhaseHints) {
                    if (testHintHP.inHalfPlane(intersection.getCoordinate())) {
                        newPolygonPoints.add(intersection);
                    }
                }
            }
        }

        // calculate new intersections and look if they are inside the polygon
        for (HalfPlaneHint currentHint : oldObtainedHints) {
            for (HalfPlaneHint testIntersectionHint : newObtainedHints) {
                addIntersectionIfInPoly(currentHint, testIntersectionHint, oldObtainedHints,
                        newObtainedHints, phaseHints, newPolygonPoints);
            }
            for (HalfPlaneHint testIntersectionHint : phaseHints) {
                addIntersectionIfInPoly(currentHint, testIntersectionHint, oldObtainedHints,
                        newObtainedHints, phaseHints, newPolygonPoints);
            }
        }
        for (HalfPlaneHint currentHint : newObtainedHints) {
            for (HalfPlaneHint testIntersectionHint : newObtainedHints) {
                addIntersectionIfInPoly(currentHint, testIntersectionHint, oldObtainedHints,
                        newObtainedHints, phaseHints, newPolygonPoints);
            }
            for (HalfPlaneHint testIntersectionHint : phaseHints) {
                addIntersectionIfInPoly(currentHint, testIntersectionHint, oldObtainedHints,
                        newObtainedHints, phaseHints, newPolygonPoints);
            }
        }
        for (HalfPlaneHint currentHint : phaseHints) {
            for (HalfPlaneHint testIntersectionHint : phaseHints) {
                addIntersectionIfInPoly(currentHint, testIntersectionHint, oldObtainedHints,
                        newObtainedHints, phaseHints, newPolygonPoints);
            }
        }
        polygonPoints = newPolygonPoints;
    }

    private void updatePhaseHints() {
        Coordinate[] phaseRectangle = phaseRectangle(phase);
        phaseHints.set(0, new HalfPlaneHint(phaseRectangle[1], phaseRectangle[0]));
        phaseHints.set(1, new HalfPlaneHint(phaseRectangle[2], phaseRectangle[1]));
        phaseHints.set(2, new HalfPlaneHint(phaseRectangle[3], phaseRectangle[2]));
        phaseHints.set(3, new HalfPlaneHint(phaseRectangle[0], phaseRectangle[3]));
    }

    private Coordinate[] phaseRectangle(int phase) {
        Coordinate[] rectangle = strategyFromPaper.phaseRectangle(phase);
        for (int i = 0; i < 4; i++) {
            rectangle[i] = transformForPaper(rectangle[i]);
        }
        return rectangle;
    }

    private void scanCurrentRectangle(Movement move) {
        RoutinesFromPaper.rectangleScan(strategyFromPaper.searchAreaCornerA, strategyFromPaper.searchAreaCornerB,
                strategyFromPaper.searchAreaCornerC, strategyFromPaper.searchAreaCornerD, move);
    }

    private Coordinate transformForPaper(double x, double y) {
        return forPaper.transform(new Coordinate(
                x - searcherStartPosition.getX(),
                y - searcherStartPosition.getY()), new Coordinate());
    }

    private Coordinate transformForPaper(Coordinate c) {
        return transformForPaper(c.x, c.y);
    }

    private Point transformForPaper(Point p) {
        return JTSUtils.GEOMETRY_FACTORY.createPoint(transformForPaper(p.getCoordinate()));
    }

    private HalfPlaneHint transformForPaper(HalfPlaneHint hint) {
        HalfPlaneHint lastHint = null;
        if (hint.getLastHint() != null) {
            lastHint = new HalfPlaneHint(
                    forPaper.transform(transformForPaper(hint.getLastHint().getCenter()), new Coordinate()),
                    forPaper.transform(transformForPaper(hint.getLastHint().getRight()), new Coordinate())
            );
        }
        return new HalfPlaneHint(
                forPaper.transform(transformForPaper(hint.getCenter()), new Coordinate()),
                forPaper.transform(transformForPaper(hint.getRight()), new Coordinate()),
                lastHint
        );
    }

    private Point transformFromPaper(Point point) {
        return (Point) fromPaper.transform(point);
    }

    private Movement transformFromPaper(Movement move) {
        Movement outputMove = new Movement();
        for (GeometryItem<Point> wayPoint : move.getPoints()) {
            outputMove.addWayPoint(transformFromPaper(wayPoint.getObject()));
        }
        //addState was  not called yet
        return strategyFromPaper.moveReturn(outputMove);
    }

    private void setABCDinStrategy() {// FIXME fehler da diese berechnung der maxima minima in den strategyFromPaper
        //                                      koordinaten passieren müsste
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        for (Intersection intersection : polygonPoints) {
            double x = intersection.getCoordinate().getX();
            double y = intersection.getCoordinate().getY();
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
        }
        strategyFromPaper.searchAreaCornerA = JTSUtils.GEOMETRY_FACTORY.createPoint(transformForPaper(
                new Coordinate(minX, maxY)));
        strategyFromPaper.searchAreaCornerB = JTSUtils.GEOMETRY_FACTORY.createPoint(transformForPaper(
                new Coordinate(maxX, maxY)));
        strategyFromPaper.searchAreaCornerC = JTSUtils.GEOMETRY_FACTORY.createPoint(transformForPaper(
                new Coordinate(maxX, minY)));
        strategyFromPaper.searchAreaCornerD = JTSUtils.GEOMETRY_FACTORY.createPoint(transformForPaper(
                new Coordinate(minX, minY)));
    }

    private void addPolygonVisualization(Movement move) {
        Coordinate[] polygonCoords = new Coordinate[polygonPoints.size() + 4];
        Coordinate[] phaseRectangle = phaseRectangle(phase);
        for (int i = 0; i < 4; i++) {
            polygonCoords[i] = phaseRectangle[i];
        }
        for (int i = 0; i < polygonPoints.size(); i++) {
            polygonCoords[i + 4] = polygonPoints.get(i + 4).getCoordinate();
        }
        Geometry convexHull = (JTSUtils.GEOMETRY_FACTORY.createMultiPointFromCoords(polygonCoords)).convexHull();
        move.addAdditionalItem(new GeometryItem<>(convexHull, GeometryType.CURRENT_POLYGON));
    }

    @Value
    private class Intersection {
        Coordinate coordinate;
        HalfPlaneHint hintOne;
        HalfPlaneHint hintTwo;
    }
}
