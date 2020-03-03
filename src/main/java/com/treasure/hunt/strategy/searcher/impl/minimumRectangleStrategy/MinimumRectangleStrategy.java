package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.StrategyFromPaper;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Value;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy.UpdatePolygonPoints.updatePolygonPoints;

/**
 * @author Rank
 */

public class MinimumRectangleStrategy extends StrategyFromPaper implements Searcher<HalfPlaneHint> {
    Point realSearcherStartPosition;
    private boolean firstMoveWithHint = true;
    private TransformStrategyFromPaper transformer;

    /**
     * received before the last update of the phase's rectangle
     */
    private List<HalfPlaneHint> oldObtainedHints;
    /**
     * received after the last update of the phase's rectangle
     */
    private List<HalfPlaneHint> newObtainedHints;
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
     * In this case, the searcher does nothing and receives a hint.
     *
     * @return {@link SearchPath} the {@link SearchPath} the searcher did
     */
    @Override
    public SearchPath move() {
        SearchPath move = new SearchPath();
        move.addPoint(realSearcherStartPosition);
        return move;
    }

    /**
     * @param hint the hint, the {@link Hider} gave last.
     * @return {@link SearchPath} the {@link SearchPath}, this searcher chose.
     */
    @Override
    public SearchPath move(HalfPlaneHint hint) {
        if (firstMoveWithHint) {
            firstMoveWithHint = false;
            transformer = new TransformStrategyFromPaper(hint, realSearcherStartPosition);
        }

        newObtainedHints.add(transformer.transformForPaper(hint));

        if (rectangleNotLargeEnough()) {
            if (currentHint != null) {
                previousHint = transformer.transformForPaper(currentHint);
            }
            currentHint = transformer.transformForPaper(hint);
            SearchPath move = new SearchPath();
            scanCurrentRectangle(move);
            Polygon newPolygon;
            do {
                ArrayList<HalfPlaneHint> oldPhaseHints = phaseHints;
                Polygon oldPhaseRectangle = phaseRectangle;
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
            return addState(transformer.transformFromPaper(move));
        } else {
            return addState(transformer.transformFromPaper(
                    super.move(transformer.transformForPaper(hint))));
        }
    }

    @Override
    protected SearchPath addState(SearchPath move) {
        //TODO alte hints einfügen
        if (transformer == null) {
            return super.addState(move);
        }
        // add polygon
        if (currentPolygon != null) {
            move.addAdditionalItem(new GeometryItem<>(transformer.transformFromPaper(currentPolygon),
                    GeometryType.CURRENT_POLYGON));
        }

        //add current and previous hint
        if (currentHint != null) {
            move.addAdditionalItem(new GeometryItem<>(
                    transformer.transformFromPaper(currentHint).getHalfPlaneLineGeometry(),
                    GeometryType.HALF_PLANE_LINE_BLUE));
        }
        if (previousHint != null) {
            move.addAdditionalItem(new GeometryItem<>(
                    transformer.transformFromPaper(previousHint).getHalfPlaneLineGeometry(),
                    GeometryType.HALF_PLANE_LINE_BROWN));
        }

        // add search rectangle and phase rectangle
        return super.addState(move, transformer.transformFromPaper(searchRectangle()),
                transformer.transformFromPaper(phaseRectangle(phase)));
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

    private void scanCurrentRectangle(SearchPath move) { // todo improve scan so that hint gets used
        RoutinesFromPaper.rectangleScan(searchAreaCornerA, searchAreaCornerB,
                searchAreaCornerC, searchAreaCornerD, move);
    }

    private void setABCDinStrategy() {
        Coordinate[] coordinatesABCD = currentPolygon.getEnvelope().getCoordinates();
        searchAreaCornerA = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[1]);
        searchAreaCornerB = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[2]);
        searchAreaCornerC = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[3]);
        searchAreaCornerD = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[0]);
        lastHintQuality = StrategyFromPaper.HintQuality.none;
    }

    @Value
    static
    class Intersection {
        Coordinate coordinate;
        HalfPlaneHint hintOne;
        HalfPlaneHint hintTwo;
    }
}
