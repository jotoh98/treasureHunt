package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.geom.StatusMessageItem;
import com.treasure.hunt.strategy.geom.StatusMessageType;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Getter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.math.Vector2D;

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
    @Getter
    private double rotation;
    @Getter
    private AffineTransformation fromAxisParallel;
    @Getter
    private AffineTransformation toAxisParallel;


    public StrategyFromPaper() {
        rotation = 0;
        // no rotation given so both transformations are the identity:
        toAxisParallel = new AffineTransformation();
        fromAxisParallel = new AffineTransformation();
    }

    /**
     * This is only used when the caller wants to get the strategy from the paper, but does not want that the
     * used rectangles are axis parallel but when rotated by -rotation they should be axis parallel.
     * (This is not useful for this particular strategy but the constructor is used in the MinimumRectangleStrategy)
     *
     * @param rotation the rotation by which the rectangles get rotated
     */
    public StrategyFromPaper(double rotation) {
        this.rotation = rotation;
        fromAxisParallel = AffineTransformation.rotationInstance(rotation);
        toAxisParallel = AffineTransformation.rotationInstance(2 * Math.PI - rotation);
    }

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
        if (rectangleNotLargeEnough()) {
            return moveReturn(addState(incrementPhase(move)));
        }
        //now analyse the hint:
        if (lastHintQuality == HintQuality.bad) {
            return moveReturn(addState(LastHintBadSubroutine.getInstance().
                    lastHintBadSubroutine(this, hint, lastBadHint, move)));
        }
        lastHintQuality = HintQuality.good; //If the current hint isn't good, the hint quality is set below again

        Point[] horizontalSplit;
        Point[] verticalSplit;
        LineSegment hintLine = hint.getHalfPlaneLine();
        if (rotation == 0) {
            horizontalSplit = splitRectangleHorizontally(searchAreaCornerA, searchAreaCornerB,
                    searchAreaCornerC, searchAreaCornerD, hint, hintLine);
            verticalSplit = splitRectangleVertically(searchAreaCornerA, searchAreaCornerB,
                    searchAreaCornerC, searchAreaCornerD, hint, hintLine);
        } else {
            horizontalSplit = splitWithRotation(
                    searchAreaCornerA, searchAreaCornerD, searchAreaCornerB, searchAreaCornerC, hint, true
            );
            verticalSplit = splitWithRotation(
                    searchAreaCornerA, searchAreaCornerB, searchAreaCornerD, searchAreaCornerC, hint, false
            );
        }

        if (horizontalSplit != null) {
            searchAreaCornerA = horizontalSplit[0];
            searchAreaCornerB = horizontalSplit[1];
            searchAreaCornerC = horizontalSplit[2];
            searchAreaCornerD = horizontalSplit[3];
            // "good" case (as defined in the paper)
            return moveReturn(addState(moveToCenterOfRectangle(searchAreaCornerA, searchAreaCornerB,
                    searchAreaCornerC, searchAreaCornerD, move)));
        }
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

    boolean rectangleNotLargeEnough() {
        double width, height;
        if (rotation == 0) {
            width = searchAreaCornerB.getX() - searchAreaCornerA.getX();
            height = searchAreaCornerA.getY() - searchAreaCornerD.getY();
        } else {
            width = searchAreaCornerA.distance(searchAreaCornerB);
            height = searchAreaCornerC.distance(searchAreaCornerD);
        }

        return (!(width >= 4) || !(height >= 4));
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
        if (rotation == 0) {
            Coordinate[] rect = currentPhaseRectangle();
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
        }
        return addState(move, curCoords, currentPhaseRectangle());
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
                Coordinate newB = new Coordinate(intersectionCDHint.getX(), B.getY()); // TODO change this
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
     * aOne and bOne must have one side of the rectangle (aOne,bOne,bTwo,aTwo) in common.
     * If horizontal is set, this method has to be called as following:
     * aOne := searchRectanglePointA
     * aTwo := searchRectanglePointD
     * bOne := searchRectanglePointB
     * bTwo := searchRectanglePointC
     * <p>
     * Otherwise this method has to be called as following:
     * aOne := searchRectanglePointA
     * aTwo := searchRectanglePointB
     * bOne := searchRectanglePointD
     * bTwo := searchRectanglePointC
     *
     * @param aOne
     * @param aTwo
     * @param bOne
     * @param bTwo
     * @param hint
     * @return
     */
    private Point[] splitWithRotation(Point aOne, Point aTwo, Point bOne, Point bTwo, HalfPlaneHint hint,
                                      boolean horizontal) {
        LineSegment hintLine = hint.getHalfPlaneLine();
        LineSegment aLine = new LineSegment(aOne.getCoordinate(), aTwo.getCoordinate());
        LineSegment bLine = new LineSegment(bOne.getCoordinate(), bTwo.getCoordinate());
        Coordinate intersectionAHint = JTSUtils.lineWayIntersection(hintLine, aLine);
        Coordinate intersectionBHint = JTSUtils.lineWayIntersection(hintLine, bLine);
        if (intersectionAHint == null || intersectionBHint == null)
            return null;
        boolean aOneInHint = hint.inHalfPlane(aOne.getCoordinate());
        boolean aTwoInHint = hint.inHalfPlane(aTwo.getCoordinate());
        boolean bOneInHint = hint.inHalfPlane(bOne.getCoordinate());
        boolean bTwoInHint = hint.inHalfPlane(bTwo.getCoordinate());
        if ((aOneInHint && (aTwoInHint || bTwoInHint)) || (bOneInHint && (aTwoInHint || bTwoInHint)))
            throw new RuntimeException("Somehow two opposing points are in the hint");
        if (aOneInHint || bOneInHint) {
            double distanceAOneHintIntersection = aOne.getCoordinate().distance(intersectionAHint);
            double distanceBOneHintIntersection = bOne.getCoordinate().distance(intersectionBHint);
            if (distanceAOneHintIntersection < distanceBOneHintIntersection) {
                Vector2D bOneToAOne = new Vector2D(bOne.getCoordinate(), aOne.getCoordinate());
                if (horizontal) {
                    return new Point[]{
                            aOne, bOne,
                            GEOMETRY_FACTORY.createPoint(intersectionBHint),
                            JTSUtils.createPoint(intersectionBHint.x + bOneToAOne.getX(),
                                    intersectionBHint.y + bOneToAOne.getY())
                    };
                } else {
                    return new Point[]{
                            aOne,
                            JTSUtils.createPoint(intersectionBHint.x + bOneToAOne.getX(),
                                    intersectionBHint.y + bOneToAOne.getY()),
                            GEOMETRY_FACTORY.createPoint(intersectionBHint),
                            bOne
                    };
                }
            } else {
                Vector2D aOneToBOne = new Vector2D(aOne.getCoordinate(), bOne.getCoordinate());
                if (horizontal) {
                    return new Point[]{
                            aOne, bOne,
                            JTSUtils.createPoint(intersectionAHint.x + aOneToBOne.getX(),
                                    intersectionAHint.y + aOneToBOne.getY()),
                            GEOMETRY_FACTORY.createPoint(intersectionAHint)
                    };
                } else {
                    return new Point[]{
                            aOne,
                            GEOMETRY_FACTORY.createPoint(intersectionAHint),
                            JTSUtils.createPoint(intersectionAHint.x + aOneToBOne.getX(),
                                    intersectionAHint.y + aOneToBOne.getY()),
                            bOne
                    };
                }
            }
        }
        if (aTwoInHint || bTwoInHint) {
            double distanceATwoHintIntersection = aTwo.getCoordinate().distance(intersectionAHint);
            double distanceBTwoHintIntersection = bTwo.getCoordinate().distance(intersectionBHint);
            if (distanceATwoHintIntersection < distanceBTwoHintIntersection) {
                Vector2D bTwoToATwo = new Vector2D(bTwo.getCoordinate(), aTwo.getCoordinate());
                if (horizontal) {
                    return new Point[]{
                            JTSUtils.createPoint(intersectionBHint.x + bTwoToATwo.getX(),
                                    intersectionBHint.y + bTwoToATwo.getY()),
                            GEOMETRY_FACTORY.createPoint(intersectionBHint),
                            bTwo,
                            aTwo
                    };
                } else {
                    return new Point[]{
                            JTSUtils.createPoint(intersectionBHint.x + bTwoToATwo.getX(),
                                    intersectionBHint.y + bTwoToATwo.getY()),
                            aTwo,
                            bTwo,
                            GEOMETRY_FACTORY.createPoint(intersectionBHint)
                    };
                }
            } else {
                Vector2D aTwoToBTwo = new Vector2D(aTwo.getCoordinate(), bTwo.getCoordinate());
                if (horizontal) {
                    return new Point[]{
                            GEOMETRY_FACTORY.createPoint(intersectionAHint),
                            JTSUtils.createPoint(intersectionAHint.x + aTwoToBTwo.getX(),
                                    intersectionAHint.y + aTwoToBTwo.getY()),
                            bTwo,
                            aTwo
                    };
                } else {
                    return new Point[]{
                            GEOMETRY_FACTORY.createPoint(intersectionAHint),
                            aTwo,
                            bTwo,
                            JTSUtils.createPoint(intersectionAHint.x + aTwoToBTwo.getX(),
                                    intersectionAHint.y + aTwoToBTwo.getY())
                    };
                }
            }
        }
        throw new RuntimeException("The hint-line does not touch the current rectangle.");
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
    private Coordinate[] currentPhaseRectangle() {
        return phaseRectangle(phase);
    }

    /**
     * Returnes the rectangle of the current phase, by using a specified phase index
     *
     * @param phase the phase to which the phase's rectangle should be returned
     * @return the points of the phase's rectangle
     */
    Coordinate[] phaseRectangle(int phase) {
        double halfDiff = Math.pow(2, phase - 1);
        double startX = start.getX();
        double startY = start.getY();
        Coordinate[] rect = new Coordinate[4];
        rect[0] = fromAxisParallel.transform(new Coordinate(startX - halfDiff, startY + halfDiff), new Coordinate());
        rect[1] = fromAxisParallel.transform(new Coordinate(startX + halfDiff, startY + halfDiff), new Coordinate());
        rect[2] = fromAxisParallel.transform(new Coordinate(startX + halfDiff, startY - halfDiff), new Coordinate());
        rect[3] = fromAxisParallel.transform(new Coordinate(startX - halfDiff, startY - halfDiff), new Coordinate());
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

}
