package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.geom.StatusMessageItem;
import com.treasure.hunt.strategy.geom.StatusMessageType;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

import static com.treasure.hunt.strategy.geom.GeometryType.*;
import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper.rectangleScan;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.StatusMessages.explainingStrategyMessage;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.StatusMessages.visualisationMessage;
import static com.treasure.hunt.utils.JTSUtils.GEOMETRY_FACTORY;
import static com.treasure.hunt.utils.JTSUtils.lineWayIntersection;

/**
 * This strategy implements the strategy from the paper "Deterministic Treasure Hunt in the Plane with Angular Hints"
 * from Bouchard et al..
 * <p>
 * Generally this strategy works in phases i=1, 2, ... in which it searchers the treasure in rectangles of the side
 * length 2^i.
 * The rectangles are centered in the start position of the searcher and are axis parallel.
 * We will call the rectangle of the current phase the phase rectangle.
 * The strategy uses a second rectangle, called the current rectangle, which equals the phase rectangle at the beginning
 * of each phase.
 * In some draws (most draws) the searcher can exclude a part of the current rectangle by using areas seen and the current hint
 * gotten and lower its area.
 * The previous rectangle is the current rectangle from the previous draw.
 * When the current rectangle is small enough, the rectangle gets scanned which means the player walks a route in such a way
 * that it sees all points of the current rectangle.
 * When this happens the phase is incremented and the current rectangle is again set to the phase rectangle.
 * <p>
 * For more information please look in the paper.
 *
 * @author Rank
 */
public class StrategyFromPaper implements Searcher<HalfPlaneHint> {
    /**
     * phase equals i in Algorithm2 (TreasureHunt1) in the paper.
     * In phase k, the algorithm checks, if the treasure is located in a rectangle
     * with a side length of 2^k, centered at the initial position of the searcher.
     */
    protected int phase;
    /**
     * searchCornerPointA, -B, -C and -D are the corners of the rectangle where the treasure is currently searched.
     * This rectangle always lies in the rectangle of the current phase.
     * The rectangle has the same function like the rectangle Ri in Algorithm2 (TreasureHunt1)
     * in the paper.
     * It is referred to as current search rectangle throughout the implementation.
     */
    protected Point searchAreaCornerA, searchAreaCornerB, searchAreaCornerC, searchAreaCornerD;
    protected HalfPlaneHint previousHint;
    protected HalfPlaneHint currentHint;
    protected HintQuality previousHintQuality = HintQuality.none;
    protected LastHintBadSubroutine lastHintBadSubroutine = new LastHintBadSubroutine(this);
    protected boolean phaseGotIncrementedThisMove = false;
    Point start; // the initial position of the player
    List<StatusMessageItem> statusMessageItemsToBeRemovedNextMove = new ArrayList<>();
    List<GeometryItem> geometryItemsToBeAddedNextMove = new ArrayList<>();
    Point lastPosition;
    GeometryItem<Polygon> lastMovesRectangle = null;

    /**
     * {@inheritDoc}
     */
    public void init(Point startPosition) {
        start = startPosition;
        phase = 1;
        setRectToPhase();
        currentHint = null;
        previousHint = null;
        lastPosition = start;
    }

    @Override
    public SearchPath move() {
        SearchPath move = new SearchPath();
        move.getStatusMessageItemsToBeAdded().add(visualisationMessage);
        move.getStatusMessageItemsToBeAdded().add(explainingStrategyMessage);
        statusMessageItemsToBeRemovedNextMove.add(visualisationMessage);
        statusMessageItemsToBeRemovedNextMove.add(explainingStrategyMessage);
        setRectToPhase();
        return (returnHandling(incrementPhase(move)));
    }

    @Override
    public SearchPath move(HalfPlaneHint hint) {
        previousHint = currentHint;
        currentHint = hint;
        phaseGotIncrementedThisMove = false;

        SearchPath move = new SearchPath();

        // remove old status messages
        move.getStatusMessageItemsToBeRemoved().addAll(statusMessageItemsToBeRemovedNextMove);
        statusMessageItemsToBeRemovedNextMove.clear();
        // add new geometry items
        for (GeometryItem item : geometryItemsToBeAddedNextMove) {
            move.addAdditionalItem(item);
        }
        geometryItemsToBeAddedNextMove.clear();

        StatusMessageItem lastHintQualityStatus;
        switch (previousHintQuality) {
            case bad:
                previousHintQuality = HintQuality.none;
                move.getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.BEFORE_PREVIOUS_QUALITY,
                        "bad"));
                move.getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.PREVIOUS_HINT_QUALITY,
                        "none, because the hint before this hint was bad."));
                SearchPath lastHintBadSteps = lastHintBadSubroutine.lastHintBadSubroutine(hint, previousHint, move, true);
                moveToCenterOfRectangle(searchAreaCornerA, searchAreaCornerB, searchAreaCornerC, searchAreaCornerD,
                        lastHintBadSteps);
                return returnHandling(lastHintBadSteps);
            case good:
                lastHintQualityStatus = new StatusMessageItem(StatusMessageType.BEFORE_PREVIOUS_QUALITY, "good");
                break;
            case none:
                lastHintQualityStatus = new StatusMessageItem(StatusMessageType.BEFORE_PREVIOUS_QUALITY, "none");
                break;
            default:
                throw new AssertionError("The hint before the previous hint has no quality value");
        }
        move.getStatusMessageItemsToBeAdded().add(lastHintQualityStatus);

        if (rectangleNotLargeEnough()) {
            previousHintQuality = HintQuality.none;
            move.getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.PREVIOUS_HINT_QUALITY,
                    "none, because the previous rectangle was small enough to be scanned directly."));
            return returnHandling(incrementPhase(move));
        }

        //now analyse the hint:
        LineSegment hintLine = hint.getHalfPlaneLine();

        Point[] horizontalSplit = splitRectangleHorizontally(searchAreaCornerA, searchAreaCornerB,
                searchAreaCornerC, searchAreaCornerD, hint, hintLine);
        if (horizontalSplit != null) {
            previousHintQuality = HintQuality.good;
            move.getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.PREVIOUS_HINT_QUALITY, "good"));
            searchAreaCornerA = horizontalSplit[0];
            searchAreaCornerB = horizontalSplit[1];
            searchAreaCornerC = horizontalSplit[2];
            searchAreaCornerD = horizontalSplit[3];
            // "good" case (as defined in the paper)
            return returnHandling(moveToCenterOfRectangle(searchAreaCornerA, searchAreaCornerB,
                    searchAreaCornerC, searchAreaCornerD, move));
        }
        Point[] verticalSplit = splitRectangleVertically(searchAreaCornerA, searchAreaCornerB,
                searchAreaCornerC, searchAreaCornerD, hint, hintLine);
        if (verticalSplit != null) {
            previousHintQuality = HintQuality.good;
            move.getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.PREVIOUS_HINT_QUALITY, "good"));
            searchAreaCornerA = verticalSplit[0];
            searchAreaCornerB = verticalSplit[1];
            searchAreaCornerC = verticalSplit[2];
            searchAreaCornerD = verticalSplit[3];
            // "good" case (as defined in the paper)
            return returnHandling(moveToCenterOfRectangle(searchAreaCornerA, searchAreaCornerB,
                    searchAreaCornerC, searchAreaCornerD, move));
        }
        // when none of this cases takes place, the hint is bad (as defined in the paper). This gets handled here:
        previousHintQuality = HintQuality.bad;
        move.getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.PREVIOUS_HINT_QUALITY,
                "bad"));

        Point destination = GEOMETRY_FACTORY.createPoint(twoStepsOrthogonal(hint,
                centerOfRectangle(searchAreaCornerA, searchAreaCornerB, searchAreaCornerC, searchAreaCornerD)));
        move.addPoint(destination);
        return returnHandling(move);
    }

    protected boolean rectangleNotLargeEnough() {
        double width = searchAreaCornerB.getX() - searchAreaCornerA.getX();
        double height = searchAreaCornerA.getY() - searchAreaCornerD.getY();
        return (!(width >= 4) || !(height >= 4));
    }

    /**
     * This method is used to visualize the current phases rectangle and the current search rectangle.
     * Adds their values to move
     *
     * @param move
     * @param currentRectanglePoints
     * @param phaseRectanglePoints
     * @return the input with the visualisations of the current phase and the search rectangle added
     */
    protected SearchPath addState(SearchPath move, Coordinate[] currentRectanglePoints, Coordinate[] phaseRectanglePoints) {
        // add previous rectangle
        if (!phaseGotIncrementedThisMove && lastMovesRectangle != null) {
            move.addAdditionalItem(lastMovesRectangle);
        } else {
            move.addAdditionalItem(new GeometryItem<>(GEOMETRY_FACTORY.createPolygon(), PREVIOUS_RECTANGLE));
        }

        // add current rectangle which the strategy is working on
        if (currentRectanglePoints != null) {
            Coordinate[] currentRectangleCoordinates = new Coordinate[5];
            for (int i = 0; i < 4; i++) {
                currentRectangleCoordinates[i] = currentRectanglePoints[i];
            }
            currentRectangleCoordinates[4] = currentRectanglePoints[0];
            GeometryItem<Polygon> curPoly = new GeometryItem<>(GEOMETRY_FACTORY.createPolygon(currentRectangleCoordinates), CURRENT_RECTANGLE);
            move.addAdditionalItem(curPoly);
            lastMovesRectangle = new GeometryItem<>(GEOMETRY_FACTORY.createPolygon(currentRectangleCoordinates), PREVIOUS_RECTANGLE);
        } else {
            GeometryItem<Polygon> curPoly = new GeometryItem<>(GEOMETRY_FACTORY.createPolygon(), CURRENT_RECTANGLE);
            move.addAdditionalItem(curPoly);
            lastMovesRectangle = new GeometryItem<>(GEOMETRY_FACTORY.createPolygon(), PREVIOUS_RECTANGLE);
        }

        // add the rectangle of the current phase
        Coordinate[] phasePolygon = new Coordinate[5];
        for (int i = 0; i < 4; i++) {
            phasePolygon[i] = phaseRectanglePoints[i];
        }
        phasePolygon[4] = phaseRectanglePoints[0];
        GeometryItem<Polygon> phase = new GeometryItem<>(GEOMETRY_FACTORY.createPolygon(phasePolygon), CURRENT_PHASE);
        move.addAdditionalItem(phase);

        return move;
    }

    protected SearchPath returnHandling(SearchPath move) {
        Coordinate[] currentCoordinates = new Coordinate[4];
        currentCoordinates[0] = searchAreaCornerA.getCoordinate();
        currentCoordinates[1] = searchAreaCornerB.getCoordinate();
        currentCoordinates[2] = searchAreaCornerC.getCoordinate();
        currentCoordinates[3] = searchAreaCornerD.getCoordinate();

        //add hints
        if (currentHint != null) {
            move.addAdditionalItem(new GeometryItem<>(currentHint.getHalfPlaneTheTreasureIsNotIn(),
                    GeometryType.HALF_PLANE_PREVIOUS_BROWN));
        }
        if (previousHint != null) {
            move.addAdditionalItem(new GeometryItem<>(previousHint.getHalfPlaneTheTreasureIsNotIn(),
                    GeometryType.HALF_PLANE_BEFORE_PREVIOUS_LIGHT_BROWN));
        }
        lastPosition = move.getLastPoint();
        return addState(move, currentCoordinates, currentPhaseRectangle());
    }

    /**
     * If the checkIfHintGood is true and the hint is bad (i.e. does not divide one side of the rectangle ABCD
     * in two parts such that both are bigger or equal to 1), null is returned.
     * If the hint-line goes through AD and BC, the biggest axis parallel-rectangle which
     * lies in ABCD and where the treasure could be located due to the information gained by the hint, is returned.
     * Otherwise the return value is null.
     *
     * @param A
     * @param B
     * @param C
     * @param D
     * @param hint
     * @param hintLine
     * @return
     */
    protected Point[] splitRectangleHorizontally(Point A, Point B, Point C, Point D, HalfPlaneHint hint,
                                                 LineSegment hintLine) {
        LineSegment BC = new LineSegment(searchAreaCornerB.getCoordinate(), searchAreaCornerC.getCoordinate());
        LineSegment AD = new LineSegment(searchAreaCornerA.getCoordinate(), searchAreaCornerD.getCoordinate());

        Coordinate intersectionADHint = JTSUtils.lineWayIntersection(hintLine, AD);
        Coordinate intersectionBCHint = JTSUtils.lineWayIntersection(hintLine, BC);

        if (intersectionADHint == null || intersectionBCHint == null ||
                ((intersectionADHint.distance(A.getCoordinate()) < 1
                        || intersectionADHint.distance(D.getCoordinate()) < 1
                        || intersectionBCHint.distance(B.getCoordinate()) < 1
                        || intersectionBCHint.distance(C.getCoordinate()) < 1))
        ) {
            return null;
        }

        if (hint.getDirection() == up) {
            Coordinate newC = intersectionBCHint;
            Coordinate newD = intersectionADHint;
            return new Point[]{A, B, GEOMETRY_FACTORY.createPoint(newC), GEOMETRY_FACTORY.createPoint(newD)};
        }

        if (hint.getDirection() == down) {
            Coordinate newA = intersectionADHint;
            Coordinate newB = intersectionBCHint;
            return new Point[]{GEOMETRY_FACTORY.createPoint(newA), GEOMETRY_FACTORY.createPoint(newB), C, D};
        }

        if (hint.pointsUpwards()) {
            if (intersectionADHint.distance(D.getCoordinate())
                    >= intersectionBCHint.distance(C.getCoordinate())) {
                Coordinate newD = new Coordinate(D.getX(), intersectionBCHint.getY());
                Coordinate newC = intersectionBCHint;
                return new Point[]{A, B, GEOMETRY_FACTORY.createPoint(newC), GEOMETRY_FACTORY.createPoint(newD)};
            } else {
                Coordinate newC = new Coordinate(C.getX(), intersectionADHint.getY());
                Coordinate newD = intersectionADHint;
                return new Point[]{A, B, GEOMETRY_FACTORY.createPoint(newC), GEOMETRY_FACTORY.createPoint(newD)};
            }
        }
        if (hint.pointsDownwards()) {
            if (intersectionADHint.distance(A.getCoordinate()) >= intersectionBCHint.distance(B.getCoordinate())) {
                Coordinate newA = new Coordinate(A.getX(), intersectionBCHint.getY());
                Coordinate newB = intersectionBCHint;
                return new Point[]{GEOMETRY_FACTORY.createPoint(newA), GEOMETRY_FACTORY.createPoint(newB), C, D};
            } else {
                Coordinate newB = new Coordinate(B.getX(), intersectionADHint.getY());
                Coordinate newA = intersectionADHint;
                return new Point[]{GEOMETRY_FACTORY.createPoint(newA), GEOMETRY_FACTORY.createPoint(newB), C, D};
            }
        }
        return null;
    }

    /**
     * A specific rectangle scanner for this strategy, in case of StrategyFromPaper, the standard rectangleScanSpecificForStrategy from
     * RoutinesFromPaper is used (this method is required for the MinimumRectangleStrategy which inherits from this class.)
     *
     * @param rectangleCorner1
     * @param rectangleCorner2
     * @param rectangleCorner3
     * @param rectangleCorner4
     * @param move
     * @return
     */
    protected SearchPath specificRectangleScan(Coordinate rectangleCorner1, Coordinate rectangleCorner2,
                                               Coordinate rectangleCorner3, Coordinate rectangleCorner4, SearchPath move) {
        return rectangleScan(rectangleCorner1, rectangleCorner2, rectangleCorner3, rectangleCorner4, move, lastPosition);
    }

    /**
     * A specific rectangle scanner for this strategy, in case of StrategyFromPaper, the standard rectangleScanSpecificForStrategy from
     * RoutinesFromPaper is used (this method is required for the MinimumRectangleStrategy which inherits from this class.)
     *
     * @param rectangleCorner1
     * @param rectangleCorner2
     * @param rectangleCorner3
     * @param rectangleCorner4
     * @param move
     * @return
     */
    protected SearchPath specificRectangleScan(Point rectangleCorner1, Point rectangleCorner2,
                                               Point rectangleCorner3, Point rectangleCorner4, SearchPath move) {
        return specificRectangleScan(rectangleCorner1.getCoordinate(), rectangleCorner2.getCoordinate(),
                rectangleCorner3.getCoordinate(), rectangleCorner4.getCoordinate(), move);
    }

    /**
     * If the checkIfHintGood is true and the hint is bad (i.e. does not divide one side of the rectangle ABCD
     * in two parts such that both are bigger or equal to 1), null is returned.
     * If the hint-line goes through AB and CD, the biggest axis-parallel rectangle which
     * lies in ABCD and where the treasure could be located due to the information gained by the hint, is returned.
     * Otherwise the return value is null.
     *
     * @param A
     * @param B
     * @param C
     * @param D
     * @param hint
     * @param hintLine
     * @return
     */
    protected Point[] splitRectangleVertically(Point A, Point B, Point C, Point D, HalfPlaneHint hint,
                                               LineSegment hintLine) {
        LineSegment AB = new LineSegment(searchAreaCornerA.getCoordinate(), searchAreaCornerB.getCoordinate());
        LineSegment CD = new LineSegment(searchAreaCornerC.getCoordinate(), searchAreaCornerD.getCoordinate());

        Coordinate intersectionABHint = lineWayIntersection(hintLine, AB);
        Coordinate intersectionCDHint = lineWayIntersection(hintLine, CD);

        // checks if the hint is good, if checkIfHintGood is true
        if (intersectionABHint == null || intersectionCDHint == null ||
                ((intersectionABHint.distance(A.getCoordinate()) < 1
                        || intersectionABHint.distance(B.getCoordinate()) < 1
                        || intersectionCDHint.distance(C.getCoordinate()) < 1
                        || intersectionCDHint.distance(D.getCoordinate()) < 1))
        ) {
            return null;
        }

        if (hint.getDirection() == left) {
            // determine which intersection-point has to be used to calculate the rectangle-points:
            if (intersectionABHint.distance(B.getCoordinate()) >= intersectionCDHint.distance(C.getCoordinate())) {
                Coordinate newB = new Coordinate(intersectionCDHint.getX(), B.getY());
                Coordinate newC = intersectionCDHint;
                return new Point[]{A, GEOMETRY_FACTORY.createPoint(newB), GEOMETRY_FACTORY.createPoint(newC), D};
            } else {
                Coordinate newC = new Coordinate(intersectionABHint.getX(), C.getY());
                Coordinate newB = intersectionABHint;
                return new Point[]{A, GEOMETRY_FACTORY.createPoint(newB), GEOMETRY_FACTORY.createPoint(newC), D};
            }
        }

        if (hint.getDirection() == right) {
            // determine which intersection-point has to be used to calculate the rectangle-points:
            if (intersectionABHint.distance(A.getCoordinate()) >= intersectionCDHint.distance(D.getCoordinate())) {
                Coordinate newA = new Coordinate(intersectionCDHint.getX(), A.getY());
                Coordinate newD = intersectionCDHint;
                return new Point[]{GEOMETRY_FACTORY.createPoint(newA), B, C, GEOMETRY_FACTORY.createPoint(newD)};
            } else {
                Coordinate newD = new Coordinate(intersectionABHint.getX(), D.getY());
                Coordinate newA = intersectionABHint;
                return new Point[]{GEOMETRY_FACTORY.createPoint(newA), B, C, GEOMETRY_FACTORY.createPoint(newD)};
            }
        }
        return null;
    }

    /**
     * Increments the phase-field and updates ABCD accordingly.
     * Then adds the step to the center of the new rectangle ABCD to move.
     *
     * @param move
     * @return the parameter move with the center of the new ABCD added
     */
    private SearchPath incrementPhase(SearchPath move) {
        phase++;
        Point oldA = searchAreaCornerA;
        Point oldB = searchAreaCornerB;
        Point oldC = searchAreaCornerC;
        Point oldD = searchAreaCornerD;
        setRectToPhase();
        specificRectangleScan(oldA, oldB, oldC, oldD, move);
        move.addPoint(centerOfRectangle(searchAreaCornerA, searchAreaCornerB, searchAreaCornerC, searchAreaCornerD));
        phaseGotIncrementedThisMove = true;
        return move;
    }

    /**
     * Returnes the rectangle of the current phase, by using the current phase index (equates to j in the paper or
     * the phase-field in this implementation)
     *
     * @return
     */
    protected Coordinate[] currentPhaseRectangle() {
        return phaseRectangle(phase);
    }

    /**
     * Returnes the rectangle of the current phase, by using a specified phase index
     *
     * @param phase the phase to which the phase's rectangle should be returned
     * @return the points of the phase's rectangle
     */
    protected Coordinate[] phaseRectangle(int phase) {
        double halfDiff = Math.pow(2, phase - 1);
        double startX = start.getX();
        double startY = start.getY();
        Coordinate[] rect = new Coordinate[4];
        rect[0] = new Coordinate(startX - halfDiff, startY + halfDiff);
        rect[1] = new Coordinate(startX + halfDiff, startY + halfDiff);
        rect[2] = new Coordinate(startX + halfDiff, startY - halfDiff);
        rect[3] = new Coordinate(startX - halfDiff, startY - halfDiff);
        return rect;
    }

    /**
     * Sets the rectangle ABCD to the rectangle of the current phase (determined by phaseRectangle())
     */
    private void setRectToPhase() {
        Coordinate[] rect = currentPhaseRectangle();
        searchAreaCornerA = GEOMETRY_FACTORY.createPoint(rect[0]);
        searchAreaCornerB = GEOMETRY_FACTORY.createPoint(rect[1]);
        searchAreaCornerC = GEOMETRY_FACTORY.createPoint(rect[2]);
        searchAreaCornerD = GEOMETRY_FACTORY.createPoint(rect[3]);
    }

    public enum HintQuality {
        good, bad, none
    }
}
