package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.StrategyFromPaper;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Value;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy.UpdatePolygonPoints.updatePolygonPoints;

public class MinimumRectangleStrategy extends StrategyFromPaper implements Searcher<HalfPlaneHint> {
    Point realSearcherStartPosition;
    private boolean firstMoveWithHint = true;
    private AffineTransformation fromPaper;
    private AffineTransformation forPaper;
    private TransformStrategyFromPaper transformer;

    private List<HalfPlaneHint> oldObtainedHints;// received before the last update of the phase's rectangle
    private List<HalfPlaneHint> newObtainedHints;// received after the last update of the phase's rectangle
    /**
     * This points represent the polygon where the treasure must lie in if it is in the current search rectangle,
     * according to all obtained hints.
     * If this List is empty, the treasure is not in the current search rectangle.
     */
    private List<Intersection> polygonPoints;
    private Polygon currentPolygon;
    private Polygon phaseRectangle;
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
        super.init(JTSUtils.createPoint(0, 0));
        this.realSearcherStartPosition = searcherStartPosition;
        phase = 1;
        phaseHints = new ArrayList<>(4);
        {
            phaseHints.add(null);
            phaseHints.add(null);
            phaseHints.add(null);
            phaseHints.add(null);
        }
        oldObtainedHints = new ArrayList<>();
        newObtainedHints = new ArrayList<>();
        updatePhaseRectangle();
        polygonPoints = new LinkedList<>();
        currentPolygon = JTSUtils.GEOMETRY_FACTORY.createPolygon();
    }

    private void updatePhaseRectangle() {
        Coordinate[] phaseRectangleCorners = currentPhaseRectangle();
        Coordinate[] tmpCornersCurrentPhaseRectangle = new Coordinate[phaseRectangleCorners.length + 1];
        System.arraycopy(phaseRectangleCorners, 0, tmpCornersCurrentPhaseRectangle, 0,
                phaseRectangleCorners.length);
        tmpCornersCurrentPhaseRectangle[phaseRectangleCorners.length] = phaseRectangleCorners[0];
        phaseRectangle = JTSUtils.GEOMETRY_FACTORY.createPolygon(tmpCornersCurrentPhaseRectangle);
    }

    /**
     * Use this to perform a initial move, without a hint given.
     * This is for the case, the searcher starts. (as he does normally)
     *
     * @return {@link Movement} the {@link Movement} the searcher did
     */
    @Override
    public Movement move() {
        Movement strategyFromPaperMovement = super.move();
        Movement transformedMove = new Movement();
        for (GeometryItem<Point> wayPoint : strategyFromPaperMovement.getPoints()) {
            transformedMove.addWayPoint(JTSUtils.createPoint(
                    wayPoint.getObject().getX() + realSearcherStartPosition.getX(),
                    wayPoint.getObject().getY() + realSearcherStartPosition.getY()
            ));
        }
        Coordinate[] currentPhaseRectangle = currentPhaseRectangle();
        for (int i = 0; i < currentPhaseRectangle.length; i++) {
            currentPhaseRectangle[i].setX(currentPhaseRectangle[i].x + realSearcherStartPosition.getX());
            currentPhaseRectangle[i].setY(currentPhaseRectangle[i].y + realSearcherStartPosition.getY());
        }
        super.addState(transformedMove, currentPhaseRectangle, currentPhaseRectangle);
        return moveReturn(transformedMove);
    }

    /**
     * @param hint the hint, the {@link Hider} gave last.
     * @return {@link Movement} the {@link Movement}, this searcher chose.
     */
    @Override
    public Movement move(HalfPlaneHint hint) {
        if (firstMoveWithHint) {
            firstMoveWithHint = false;
            transformer = new TransformStrategyFromPaper(hint, realSearcherStartPosition, this);
            HalfPlaneHint transformedHint = new HalfPlaneHint(new Coordinate(0, 0), new Coordinate(1, 0));
            newObtainedHints.add(transformedHint);
            Movement move = move(transformedHint);
            lastLocation = (move.getEndPoint()); // set last location accoringly
            return moveReturnMinimumRectangleStrategy(transformer.transformFromPaper(move));
            // the initial input hint for the strategy from the paper by definition shows upwards (in this strategy)
        }

        newObtainedHints.add(transformer.transformForPaper(hint));

        Polygon newPolygon;
        if (rectangleNotLargeEnough()) {
            Movement move = new Movement();
            move.addWayPoint(transformer.transformFromPaper(lastLocation)); // the first point has to be the last location of the player
            scanCurrentRectangle(move);
            ArrayList<HalfPlaneHint> oldPhaseHints = phaseHints;
            Polygon oldPhaseRectangle = phaseRectangle;
            do {
                phase++;
                updatePhaseHints();
                updatePhaseRectangle();
                newPolygon = updatePolygonPoints(polygonPoints, oldObtainedHints, newObtainedHints, oldPhaseHints,
                        phaseHints, oldPhaseRectangle);
            }
            while (newPolygon == null);
            currentPolygon = newPolygon;

            setABCDinStrategy();
            GeometricUtils.moveToCenterOfRectangle(searchAreaCornerA, searchAreaCornerB, searchAreaCornerC,
                    searchAreaCornerD, move);
            oldObtainedHints.addAll(newObtainedHints);
            newObtainedHints.clear();
            lastLocation = transformer.transformForPaper(move.getEndPoint()); // set last location accoringly
            return moveReturnMinimumRectangleStrategy(addState(move));
        } else
            return moveReturnMinimumRectangleStrategy(addState(transformer.transformFromPaper(
                    super.move(transformer.transformForPaper(hint)))));
    }

    private Movement moveReturnMinimumRectangleStrategy(Movement move) {
        Point strategyLastLocation = lastLocation;
        Movement ret = moveReturn(move);
        lastLocation = strategyLastLocation;
        return ret;
    }

    @Override
    protected Movement addState(Movement move) {
        if (transformer == null)
            return super.addState(move);
        // add polygon
        move.addAdditionalItem(new GeometryItem<>(currentPolygon, GeometryType.CURRENT_POLYGON));
        // add search rectangle and phase rectangle
        return super.addState(move, transformer.transformFromPaper(searchRectangle()),
                transformer.transformFromPaper(phaseRectangle(phase)));
    }

    @Override
    public Movement moveReturn(Movement move) {
        return super.moveReturn(move);
    }

    private Coordinate[] searchRectangle() {
        return new Coordinate[]{searchAreaCornerA.getCoordinate(), searchAreaCornerB.getCoordinate(),
                searchAreaCornerC.getCoordinate(), searchAreaCornerD.getCoordinate()};
    }

    private void updatePhaseHints() {
        Coordinate[] phaseRectangle = phaseRectangle(phase);
        phaseHints.set(0, new HalfPlaneHint(phaseRectangle[1], phaseRectangle[0]));
        phaseHints.set(1, new HalfPlaneHint(phaseRectangle[2], phaseRectangle[1]));
        phaseHints.set(2, new HalfPlaneHint(phaseRectangle[3], phaseRectangle[2]));
        phaseHints.set(3, new HalfPlaneHint(phaseRectangle[0], phaseRectangle[3]));
    }

    private void scanCurrentRectangle(Movement move) {
        RoutinesFromPaper.rectangleScan(searchAreaCornerA, searchAreaCornerB,
                searchAreaCornerC, searchAreaCornerD, move);
    }

    private void setABCDinStrategy() {
        Coordinate[] coordinatesABCD = currentPolygon.getEnvelope().getCoordinates();
        searchAreaCornerA = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[3]);
        searchAreaCornerB = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[2]);
        searchAreaCornerC = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[1]);
        searchAreaCornerD = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[0]);
    }

    @Value
    static
    class Intersection {
        Coordinate coordinate;
        HalfPlaneHint hintOne;
        HalfPlaneHint hintTwo;
    }
}
