package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.geom.StatusMessageItem;
import com.treasure.hunt.strategy.geom.StatusMessageType;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.treasure.hunt.strategy.geom.GeometryType.CURRENT_PHASE;
import static com.treasure.hunt.strategy.geom.GeometryType.CURRENT_RECTANGLE;
import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper.rectangleScan;
import static com.treasure.hunt.utils.JTSUtils.*;

enum HintQuality {
    good, bad, none
}

/**
 * This implements the strategy from the paper:
 * {@literal Treasure Hunt in the Plane with Angular Hints}
 *
 * @author bsen
 */
public class StrategyFromPaper implements Searcher<HalfPlaneHint> {
    /**
     * phase equals i in Algorithm2 (TreasureHunt1) in the paper.
     * In phase k, the algorithm checks, if the treasure is located in a rectangle
     * with a side length of 2^k, centered at the initial position of the searcher.
     */
    int phase;
    Point start, // the initial position of the player
    /**
     * searchCornerPointA, -B, -C and -D are the corners of the rectangle where the treasure is currently searched.
     * This rectangle always lies in the rectangle of the current phase.
     * The rectangle has the same function like the rectangle Ri in Algorithm2 (TreasureHunt1)
     * in the paper.
     * It is referred to as current search rectangle throughout the implementation.
     */
    searchAreaCornerA, searchAreaCornerB, searchAreaCornerC, searchAreaCornerD;
    HalfPlaneHint lastBadHint; //only used when last hint was bad
    HintQuality lastHintQuality = HintQuality.none;
    //boolean lastHintWasBad = false;
    List<StatusMessageItem> statusMessageItemsToBeRemovedNextMove = new ArrayList<>();
    private Point lastLocation;

    /**
     * {@inheritDoc}
     */
    public void init(Point startPosition) {
        start = startPosition;
        lastLocation = startPosition;
        phase = 1;
        setRectToPhase();
    }

    public void init(Point startPosition, int w, int h) {
        init(startPosition);
    }

    @Override
    public Movement move() {
        Movement move = new Movement();
        move.addWayPoint(lastLocation);
        setRectToPhase();
        return moveReturn(addState(incrementPhase(move)));
    }

    @Override
    public Movement move(HalfPlaneHint hint) {
        Movement move = new Movement();

        // remove old status messages
        move.getStatusMessageItemsToBeRemoved().addAll(statusMessageItemsToBeRemovedNextMove);
        statusMessageItemsToBeRemovedNextMove.clear();

        //update status messages:
        StatusMessageItem goodStatusMessage = new StatusMessageItem(StatusMessageType.PREVIOUS_HINT_QUALITY, "good");
        move.getStatusMessageItemsToBeAdded().add(goodStatusMessage);

        StatusMessageItem lastHintQualityStatus;
        switch (lastHintQuality) {
            case bad:
                lastHintQualityStatus = new StatusMessageItem(StatusMessageType.BEFORE_PREVIOUS_QUALITY, "bad");
                break;
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

        move.addWayPoint(lastLocation);
        double width = searchAreaCornerB.getX() - searchAreaCornerA.getX();
        double height = searchAreaCornerA.getY() - searchAreaCornerD.getY();
        if (width < 4 || height < 4) {
            return moveReturn(addState(incrementPhase(move)));
        }
        //now analyse the hint:
        if (lastHintQuality == HintQuality.bad) {
            return moveReturn(addState(LastHintBadSubroutine.getInstance().
                    lastHintBadSubroutine(this, hint, lastBadHint, move)));
        }
        lastHintQuality = HintQuality.good; //If the current hint isn't good, the hint quality is set below again

        LineSegment hintLine = new LineSegment(hint.getCenter(),
                hint.getRight());

        Point[] horizontalSplit = splitRectangleHorizontally(searchAreaCornerA, searchAreaCornerB,
                searchAreaCornerC, searchAreaCornerD, hint, hintLine);
        if (horizontalSplit != null) {
            searchAreaCornerA = horizontalSplit[0];
            searchAreaCornerB = horizontalSplit[1];
            searchAreaCornerC = horizontalSplit[2];
            searchAreaCornerD = horizontalSplit[3];
            // "good" case (as defined in the paper)
            return moveReturn(addState(moveToCenterOfRectangle(searchAreaCornerA, searchAreaCornerB,
                    searchAreaCornerC, searchAreaCornerD, move)));
        }
        Point[] verticalSplit = splitRectangleVertically(searchAreaCornerA, searchAreaCornerB,
                searchAreaCornerC, searchAreaCornerD, hint, hintLine);
        if (verticalSplit != null) {
            searchAreaCornerA = verticalSplit[0];
            searchAreaCornerB = verticalSplit[1];
            searchAreaCornerC = verticalSplit[2];
            searchAreaCornerD = verticalSplit[3];
            // "good" case (as defined in the paper)
            return moveReturn(addState(moveToCenterOfRectangle(searchAreaCornerA, searchAreaCornerB,
                    searchAreaCornerC, searchAreaCornerD, move)));
        }
        // when none of this cases takes place, the hint is bad (as defined in the paper). This gets handled here:
        move.getStatusMessageItemsToBeAdded().remove(goodStatusMessage);
        move.getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.PREVIOUS_HINT_QUALITY, "bad"));

        Point destination = GEOMETRY_FACTORY.createPoint(twoStepsOrthogonal(hint,
                centerOfRectangle(searchAreaCornerA, searchAreaCornerB, searchAreaCornerC, searchAreaCornerD)));
        move.addWayPoint(destination);
        lastHintQuality = HintQuality.bad;
        lastBadHint = hint;
        return moveReturn(addState(move));
    }

    /**
     * This method is used to visualize the current phases rectangle and the current search rectangle.
     * Adds their values to move
     *
     * @param move
     * @return the input with the visualisations of the current phase and the search rectangle added
     */
    Movement addState(Movement move, Coordinate[] currentRectanglePoints, Coordinate[] phaseRectanglePoints) {
        // add current rectangle which the strategy is working on
        Coordinate[] curCoords = new Coordinate[5];
        for (int i = 0; i < 4; i++) {
            curCoords[i] = currentRectanglePoints[i];
        }
        curCoords[4] = currentRectanglePoints[0];
        GeometryItem<Polygon> curPoly = new GeometryItem<>(GEOMETRY_FACTORY.createPolygon(curCoords), CURRENT_RECTANGLE);
        move.addAdditionalItem(curPoly);

        // add the rectangle of the current phase
        Coordinate[] phasePolygon = new Coordinate[5];
        for (int i = 0; i < 4; i++)
            phasePolygon[i] = phaseRectanglePoints[i];
        phasePolygon[4] = phaseRectanglePoints[0];
        GeometryItem<Polygon> phase = new GeometryItem<>(GEOMETRY_FACTORY.createPolygon(phasePolygon), CURRENT_PHASE);
        move.addAdditionalItem(phase);
        return move;
    }

    private Movement addState(Movement move) {
        Coordinate[] curCoords = new Coordinate[4];
        curCoords[0] = searchAreaCornerA.getCoordinate();
        curCoords[1] = searchAreaCornerB.getCoordinate();
        curCoords[2] = searchAreaCornerC.getCoordinate();
        curCoords[3] = searchAreaCornerD.getCoordinate();

        // assert if the current rectangle ABCD lies in the rectangle of the current phase
        Coordinate[] rect = phaseRectangle();
        if (
                !doubleEqual(searchAreaCornerA.getX(), rect[0].getX()) && searchAreaCornerA.getX() < rect[0].getX() ||
                        !doubleEqual(searchAreaCornerA.getX(), rect[1].getX())
                                && searchAreaCornerA.getX() > rect[1].getX() ||
                        !doubleEqual(searchAreaCornerA.getY(), rect[0].getY())
                                && searchAreaCornerA.getY() > rect[0].getY() ||
                        !doubleEqual(searchAreaCornerA.getY(), rect[2].getY())
                                && searchAreaCornerA.getY() < rect[2].getY() ||


                        !doubleEqual(searchAreaCornerB.getX(), rect[0].getX())
                                && searchAreaCornerB.getX() < rect[0].getX() ||
                        !doubleEqual(searchAreaCornerB.getX(), rect[1].getX())
                                && searchAreaCornerB.getX() > rect[1].getX() ||
                        !doubleEqual(searchAreaCornerB.getY(), rect[0].getY())
                                && searchAreaCornerB.getY() > rect[0].getY() ||
                        !doubleEqual(searchAreaCornerB.getY(), rect[2].getY())
                                && searchAreaCornerB.getY() < rect[2].getY() ||

                        !doubleEqual(searchAreaCornerC.getX(), rect[0].getX())
                                && searchAreaCornerC.getX() < rect[0].getX() ||
                        !doubleEqual(searchAreaCornerC.getX(), rect[1].getX())
                                && searchAreaCornerC.getX() > rect[1].getX() ||
                        !doubleEqual(searchAreaCornerC.getY(), rect[0].getY())
                                && searchAreaCornerC.getY() > rect[0].getY() ||
                        !doubleEqual(searchAreaCornerC.getY(), rect[2].getY())
                                && searchAreaCornerC.getY() < rect[2].getY() ||

                        !doubleEqual(searchAreaCornerD.getX(), rect[0].getX())
                                && searchAreaCornerD.getX() < rect[0].getX() ||
                        !doubleEqual(searchAreaCornerD.getX(), rect[1].getX())
                                && searchAreaCornerD.getX() > rect[1].getX() ||
                        !doubleEqual(searchAreaCornerD.getY(), rect[0].getY())
                                && searchAreaCornerD.getY() > rect[0].getY() ||
                        !doubleEqual(searchAreaCornerD.getY(), rect[2].getY())
                                && searchAreaCornerD.getY() < rect[2].getY()
        ) {
            throw new AssertionError(
                    "phaseRect:\n" +
                            rect[0].toString() + "\n" +
                            rect[1].toString() + "\n" +
                            rect[2].toString() + "\n" +
                            rect[3].toString() + "\n" +
                            "ABCD:\n"
                            + Arrays.toString(searchAreaCornerA.getCoordinates()) + "\n"
                            + Arrays.toString(searchAreaCornerB.getCoordinates()) + "\n"
                            + Arrays.toString(searchAreaCornerC.getCoordinates()) + "\n"
                            + Arrays.toString(searchAreaCornerD.getCoordinates())
            );
        }
        return addState(move, curCoords, phaseRectangle());
    }

    /**
     * This function has to be called directly before move() or move(HalfPlaneHint) returns.
     * It sets the current location accordingly and adds the lines of the way described by move.
     *
     * @param move the move to be returned by one of the two move-methods
     * @return move with lines added to the additionalGeometryItems
     */
    Movement moveReturn(Movement move) {
        List<GeometryItem<Point>> points = move.getPoints();
        Point lastPoint = null;
        for (GeometryItem g : points) {
            Point p = (Point) g.getObject();
            if (lastPoint != null) {
                LineString line = GEOMETRY_FACTORY.createLineString(
                        new Coordinate[]{lastPoint.getCoordinate(), p.getCoordinate()});
                //System.out.println("Line " + lastPoint.getCoordinate() + ", " + p.getCoordinate());//test
                move.addAdditionalItem(
                        new GeometryItem(line, GeometryType.SEARCHER_MOVEMENT)
                );
            }
            lastPoint = p;
        }
        lastLocation = move.getEndPoint();
        return move;
    }

    /**
     * If the hint-line goes through AD and BC and the hint is good (i.e. the hint divides one side of the rectangle in
     * two parts such that both are bigger or equal to 1), the biggest axis parallel-rectangle which
     * lies in ABCD and where the treasure could be located due to the information gained by the hint, is returned.
     * Otherwise the return value is null.
     *
     * @param A
     * @param B
     * @param C
     * @param D
     * @param hint
     * @return
     */
    private Point[] splitRectangleHorizontally(Point A, Point B, Point C, Point D, HalfPlaneHint hint,
                                               LineSegment hintLine) {
        LineSegment BC = new LineSegment(searchAreaCornerB.getCoordinate(), searchAreaCornerC.getCoordinate());
        LineSegment AD = new LineSegment(searchAreaCornerA.getCoordinate(), searchAreaCornerD.getCoordinate());

        Coordinate intersectionADHint = JTSUtils.lineWayIntersection(hintLine, AD);
        Coordinate intersectionBCHint = JTSUtils.lineWayIntersection(hintLine, BC);

        if (intersectionADHint == null || intersectionBCHint == null) {
            return null;
        }

        if (hint.getDirection() == up) {
            if ((intersectionADHint.getY() - D.getY()) >= 1) {
                Coordinate newC = intersectionBCHint;
                Coordinate newD = intersectionADHint;
                return new Point[]{A, B, GEOMETRY_FACTORY.createPoint(newC), GEOMETRY_FACTORY.createPoint(newD)};
            }
        }

        if (hint.getDirection() == down) {
            if ((A.getY() - intersectionADHint.getY()) >= 1) {
                Coordinate newA = intersectionADHint;
                Coordinate newB = intersectionBCHint;
                return new Point[]{GEOMETRY_FACTORY.createPoint(newA), GEOMETRY_FACTORY.createPoint(newB), C, D};
            }
        }

        if (hint.pointsUpwards()) {
            if (intersectionADHint.distance(D.getCoordinate()) >= 1
                    && intersectionBCHint.distance(C.getCoordinate()) >= 1) {
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
        }
        if (hint.pointsDownwards()) {
            if (intersectionADHint.distance(A.getCoordinate()) >= 1
                    && intersectionBCHint.distance(B.getCoordinate()) >= 1) {
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
        }
        return null;
    }

    /**
     * If the hint-line goes through AB and CD and the hint is good (i.e. the hint divides one side of the rectangle in
     * two parts such that the smaller one is bigger or equal to 1), the biggest axis-parallel rectangle which
     * lies in ABCD and where the treasure could be located due to the information gained by the hint, is returned.
     * Otherwise the return value is null.
     *
     * @param A
     * @param B
     * @param C
     * @param D
     * @param hint
     * @return
     */
    private Point[] splitRectangleVertically(Point A, Point B, Point C, Point D, HalfPlaneHint hint,
                                             LineSegment hintLine) {
        LineSegment AB = new LineSegment(searchAreaCornerA.getCoordinate(), searchAreaCornerB.getCoordinate());
        LineSegment CD = new LineSegment(searchAreaCornerC.getCoordinate(), searchAreaCornerD.getCoordinate());

        Coordinate intersectionABHint = lineWayIntersection(hintLine, AB);
        Coordinate intersectionCDHint = lineWayIntersection(hintLine, CD);

        // checks if the hint is good (i.e. if the hint divides one side of the rectangles in into two parts such that
        // the smaller one is bigger or equal to 1)
        if (intersectionABHint == null || intersectionCDHint == null
                || (intersectionABHint.distance(A.getCoordinate()) < 1
                || intersectionABHint.distance(B.getCoordinate()) < 1
                || intersectionCDHint.distance(C.getCoordinate()) < 1
                || intersectionCDHint.distance(D.getCoordinate()) < 1)) {
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
    private Movement incrementPhase(Movement move) {
        phase++;
        Point oldA = searchAreaCornerA;
        Point oldB = searchAreaCornerB;
        Point oldC = searchAreaCornerC;
        Point oldD = searchAreaCornerD;
        setRectToPhase();
        rectangleScan(oldA, oldB, oldC, oldD, move);
        move.addWayPoint(centerOfRectangle(searchAreaCornerA, searchAreaCornerB, searchAreaCornerC, searchAreaCornerD));
        return move;
    }

    /**
     * Returnes the rectangle of the current phase, by using the current phase index (equates to j in the paper or
     * the phase-field in this implementation)
     *
     * @return
     */
    private Coordinate[] phaseRectangle() {
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
        Coordinate[] rect = phaseRectangle();
        searchAreaCornerA = GEOMETRY_FACTORY.createPoint(rect[0]);
        searchAreaCornerB = GEOMETRY_FACTORY.createPoint(rect[1]);
        searchAreaCornerC = GEOMETRY_FACTORY.createPoint(rect[2]);
        searchAreaCornerD = GEOMETRY_FACTORY.createPoint(rect[3]);
    }

}
