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
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

import static com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy.ExcludedAreasUtils.intersectHints;
import static com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy.ExcludedAreasUtils.visitedPolygon;

/**
 * @author Rank
 */

public class MinimumRectangleStrategy extends StrategyFromPaper implements Searcher<HalfPlaneHint> {
    Point realSearcherStartPosition;
    private boolean firstMoveWithHint = true;
    private TransformForAxisParallelism transformer;
    /**
     * received after the last update of the phase's rectangle
     */
    private List<HalfPlaneHint> obtainedHints;
    /**
     * This points represent the polygon where the treasure must lie in if it is in the current search rectangle,
     * according to all obtained hints.
     * If this List is empty, the treasure is not in the current search rectangle.
     */
    private Geometry currentMultiPolygon;
    /**
     * This are not real obtained hints.
     * This hints are just the borders of the current phase rectangle interpreted as hints.
     * All hintslines go from one corner of the current phase rectangle to another and show in a direction that the
     * hole phase rectangle lies in the treasure area.
     */
    private ArrayList<HalfPlaneHint> phaseHints;
    /**
     * The polygon the player already has seen because he was there.
     */
    private Polygon visitedPolygon; // stored in internal coordinates
    /**
     * The lastLocation of the previous move
     */
    private Point lastLocation; // in internal coordinates

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
        obtainedHints = new ArrayList<>();
        currentMultiPolygon = JTSUtils.GEOMETRY_FACTORY.createPolygon();
        visitedPolygon = JTSUtils.GEOMETRY_FACTORY.createPolygon();
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
            transformer = new TransformForAxisParallelism(hint, realSearcherStartPosition);
            lastLocation = JTSUtils.createPoint(0, 0); // the point the searcher moved to in the previous move
            visitedPolygon.union(visitedPolygon(lastLocation, new SearchPath()));
        }

        obtainedHints.add(transformer.toInternal(hint));

        if (rectangleNotLargeEnough()) {
            if (currentHint != null) {
                previousHint = currentHint;
            }
            currentHint = transformer.toInternal(hint);
            SearchPath move = new SearchPath();
            scanCurrentRectangle(move, currentHint);
            Geometry newMultiPolygon;
            do {
                phase++;
                updatePhaseHints();
                newMultiPolygon = intersectHints(obtainedHints, phaseHints);
                if (newMultiPolygon != null) {
                    newMultiPolygon = newMultiPolygon.difference(visitedPolygon);
                }
            }
            while (newMultiPolygon == null || newMultiPolygon.getArea() == 0);
            currentMultiPolygon = newMultiPolygon;

            setABCDinStrategy();
            GeometricUtils.moveToCenterOfRectangle(searchAreaCornerA, searchAreaCornerB, searchAreaCornerC,
                    searchAreaCornerD, move);
            return returnHandling(move);
        } else {
            return returnHandling(super.move(transformer.toInternal(hint)));
        }
    }

    /**
     * This method has to be called directly before move(HalfPlaneHint) returns
     * In this method, three things are accomplished:
     * 1. the internal fields are updated (visitedPolygon, lastLocation)
     * 2. the move is transformed in the external coordinates
     * 3. the state of the current rectangle is added to the move
     *
     * @param move
     * @return
     */
    protected SearchPath returnHandling(SearchPath move) {
        // update internal state of this strategy
        visitedPolygon = (Polygon) visitedPolygon.union(visitedPolygon(lastLocation, move));
        lastLocation = move.getLastPoint();

        // the move is transformed in external coordinates
        move = transformer.toExternal(move);

        // add state of the current rectangle to move
        if (transformer == null) {
            return super.addState(move);
        }
        // add polygon
        if (currentMultiPolygon != null) {
            move.addAdditionalItem(new GeometryItem<>(transformer.toExternal(currentMultiPolygon),
                    GeometryType.CURRENT_POLYGON));
        }

        //add current and previous hint
        if (currentHint != null) {
            move.addAdditionalItem(new GeometryItem<>(
                    transformer.toExternal(currentHint).getHalfPlaneLineGeometry(),
                    GeometryType.HALF_PLANE_LINE_BLUE));
        }
        if (previousHint != null) {
            move.addAdditionalItem(new GeometryItem<>(
                    transformer.toExternal(previousHint).getHalfPlaneLineGeometry(),
                    GeometryType.HALF_PLANE_LINE_BROWN));
        }

        // add search rectangle and phase rectangle
        return super.addState(move, transformer.toExternal(searchRectangle()),
                transformer.toExternal(phaseRectangle(phase)));
    }

    @Override
    protected SearchPath specificRectangleScan(Coordinate rectangleCorner1, Coordinate rectangleCorner2,
                                               Coordinate rectangleCorner3, Coordinate rectangleCorner4, SearchPath move) {
        return rectangleScanMinimal(rectangleCorner1, rectangleCorner2, rectangleCorner3, rectangleCorner4,
                move);
    }

    private SearchPath rectangleScanMinimal(Coordinate rectangleCorner1, Coordinate rectangleCorner2,
                                            Coordinate rectangleCorner3, Coordinate rectangleCorner4, SearchPath move) {
        TransformForAxisParallelism transformerForRectangleAxisParallelism =
                new TransformForAxisParallelism(new LineSegment(rectangleCorner1, rectangleCorner2));
        ArrayList<HalfPlaneHint> rectangleToScanHints = new ArrayList<>(4);
        rectangleToScanHints.add(new HalfPlaneHint(rectangleCorner2, rectangleCorner1));
        rectangleToScanHints.add(new HalfPlaneHint(rectangleCorner3, rectangleCorner2));
        rectangleToScanHints.add(new HalfPlaneHint(rectangleCorner4, rectangleCorner3));
        rectangleToScanHints.add(new HalfPlaneHint(rectangleCorner1, rectangleCorner4));
        Polygon newPolygonToScan = intersectHints(obtainedHints, rectangleToScanHints);
        if (newPolygonToScan == null || newPolygonToScan.getArea() == 0) {
            return move;
        }
        Polygon newPolygonToScanTransformed = transformerForRectangleAxisParallelism.toInternal(newPolygonToScan);
        visitedPolygon = (Polygon) visitedPolygon.union(visitedPolygon(lastLocation, move));
        Geometry newAreaToScanTransformed = newPolygonToScanTransformed.difference(visitedPolygon);
        if(newAreaToScanTransformed.getArea()==0){
            return move;
        }

        Coordinate[] envelopeToScanTransformedPoints = newAreaToScanTransformed.getEnvelope().getCoordinates();

        RoutinesFromPaper.rectangleScan(
                transformerForRectangleAxisParallelism.toExternal(envelopeToScanTransformedPoints[0]),
                transformerForRectangleAxisParallelism.toExternal(envelopeToScanTransformedPoints[1]),
                transformerForRectangleAxisParallelism.toExternal(envelopeToScanTransformedPoints[2]),
                transformerForRectangleAxisParallelism.toExternal(envelopeToScanTransformedPoints[3]), move
        );
        return move;
    }

    private Coordinate[] searchRectangle() {
        return new Coordinate[]{searchAreaCornerA.getCoordinate(), searchAreaCornerB.getCoordinate(),
                searchAreaCornerC.getCoordinate(), searchAreaCornerD.getCoordinate()};
    }

    private void updatePhaseHints() {
        Coordinate[] phaseRectangle = phaseRectangle(phase);
        phaseHints = new ArrayList<>(4);
        phaseHints.add(new HalfPlaneHint(phaseRectangle[1], phaseRectangle[0]));
        phaseHints.add(new HalfPlaneHint(phaseRectangle[2], phaseRectangle[1]));
        phaseHints.add(new HalfPlaneHint(phaseRectangle[3], phaseRectangle[2]));
        phaseHints.add(new HalfPlaneHint(phaseRectangle[0], phaseRectangle[3]));
    }

    private void scanCurrentRectangle(SearchPath move, HalfPlaneHint hint) {
        rectangleScanMinimal(searchAreaCornerA.getCoordinate(), searchAreaCornerB.getCoordinate(),
                searchAreaCornerC.getCoordinate(), searchAreaCornerD.getCoordinate(), move);
    }

    private void setABCDinStrategy() {
        Coordinate[] coordinatesABCD = currentMultiPolygon.getEnvelope().getCoordinates();
        searchAreaCornerA = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[1]);
        searchAreaCornerB = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[2]);
        searchAreaCornerC = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[3]);
        searchAreaCornerD = JTSUtils.GEOMETRY_FACTORY.createPoint(coordinatesABCD[0]);
        lastHintQuality = StrategyFromPaper.HintQuality.none;
    }
}

