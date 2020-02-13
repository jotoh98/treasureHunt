package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.*;

import java.util.Arrays;
import java.util.List;

import static com.treasure.hunt.strategy.geom.GeometryType.CURRENT_PHASE;
import static com.treasure.hunt.strategy.geom.GeometryType.CURRENT_RECTANGLE;
import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper.rectangleScan;
import static com.treasure.hunt.utils.JTSUtils.*;

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
     * searchAreaPointA, -B, -C and -D are the corners of the rectangle where the treasure is currently searched.
     * This rectangle always lies in the rectangle of the current phase.
     * The rectangle has the same function like the rectangle Ri in Algorithm2 (TreasureHunt1)
     * in the paper.
     */
    searchAreaCornerA, searchAreaCornerB, searchAreaCornerC, searchAreaCornerD;


    HalfPlaneHint lastBadHint; //only used when last hint was bad
    boolean lastHintWasBad = false;
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

        move.addWayPoint(lastLocation);
        double width = searchAreaCornerB.getX() - searchAreaCornerA.getX();
        double height = searchAreaCornerA.getY() - searchAreaCornerD.getY();
        if (width < 4 || height < 4) {
            return moveReturn(addState(incrementPhase(move)));
        }
        //now analyse the hint:
        if (lastHintWasBad) {
            return moveReturn(addState(BadHintSubroutine.getInstance().
                    lastHintBadSubroutine(this, hint, lastBadHint, move)));
        }

        LineSegment AB = new LineSegment(searchAreaCornerA.getCoordinate(), searchAreaCornerB.getCoordinate());
        LineSegment BC = new LineSegment(searchAreaCornerB.getCoordinate(), searchAreaCornerC.getCoordinate());
        LineSegment CD = new LineSegment(searchAreaCornerC.getCoordinate(), searchAreaCornerD.getCoordinate());
        LineSegment AD = new LineSegment(searchAreaCornerA.getCoordinate(), searchAreaCornerD.getCoordinate());

        LineSegment hintLine = new LineSegment(hint.getCenter(),
                hint.getRight());

        Point intersection_AD_hint = null;
        Point intersection_BC_hint = null;
        Point intersection_AB_hint = null;
        Point intersection_CD_hint = null;
        if (lineWayIntersection(hintLine, AD) != null)
            intersection_AD_hint = GEOMETRY_FACTORY.createPoint(JTSUtils.lineWayIntersection(hintLine, AD));

        if (lineWayIntersection(hintLine, BC) != null)
            intersection_BC_hint = GEOMETRY_FACTORY.createPoint(JTSUtils.lineWayIntersection(hintLine, BC));

        if (lineWayIntersection(hintLine, AB) != null)
            intersection_AB_hint = GEOMETRY_FACTORY.createPoint(JTSUtils.lineWayIntersection(hintLine, AB));

        if (lineWayIntersection(hintLine, CD) != null)
            intersection_CD_hint = GEOMETRY_FACTORY.createPoint(JTSUtils.lineWayIntersection(hintLine, CD));

        Point[] horizontalSplit = splitRectangleHorizontally(searchAreaCornerA, searchAreaCornerB,
                searchAreaCornerC, searchAreaCornerD, hint, intersection_AD_hint, intersection_BC_hint);
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
                searchAreaCornerC, searchAreaCornerD, hint, intersection_AB_hint, intersection_CD_hint);
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
        Point destination = GEOMETRY_FACTORY.createPoint(twoStepsOrthogonal(hint,
                centerOfRectangle(searchAreaCornerA, searchAreaCornerB, searchAreaCornerC, searchAreaCornerD)));
        move.addWayPoint(destination);
        lastHintWasBad = true;
        lastBadHint = hint;
        return moveReturn(addState(move));
    }

    /**
     * This method is used to visualize the current phases rectangle and ABCD.
     * Adds their values to move
     *
     * @param move
     * @return the input with the rectangles of the current phase and ABCD added
     */
    private Movement addState(Movement move) {
        // add current rectangle which the strategy is working on
        Coordinate[] cur_coords = new Coordinate[5];
        cur_coords[0] = searchAreaCornerA.getCoordinate();
        cur_coords[1] = searchAreaCornerB.getCoordinate();
        cur_coords[2] = searchAreaCornerC.getCoordinate();
        cur_coords[3] = searchAreaCornerD.getCoordinate();
        cur_coords[4] = searchAreaCornerA.getCoordinate();

        Polygon cur_rect = GEOMETRY_FACTORY.createPolygon(cur_coords);
        GeometryItem<Polygon> cur = new GeometryItem<Polygon>(cur_rect, CURRENT_RECTANGLE);
        move.addAdditionalItem(cur);

        // add the rectangle of the current phase
        Coordinate[] phaseRect = phaseRectangle();
        Coordinate[] phasePolygon = new Coordinate[5];
        for (int i = 0; i < 4; i++)
            phasePolygon[i] = phaseRect[i];
        phasePolygon[4] = phaseRect[0];
        Polygon rect_phase = GEOMETRY_FACTORY.createPolygon(phasePolygon);
        GeometryItem<Polygon> phase = new GeometryItem<Polygon>(rect_phase, CURRENT_PHASE);
        move.addAdditionalItem(phase);

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
        return move;
    }

    /**
     * This function has to be called directly before move() or move(HalfPlaneHint) returns.
     * It sets the current location accordingly and adds the lines of the way described by move.
     *
     * @param move the move to be returned by one of the two move-methods
     * @return move with lines added to the additionalGeometryItems
     */
    private Movement moveReturn(Movement move) {
        List<GeometryItem<Point>> points = move.getPoints();
        Point lastPoint = null;
        for (GeometryItem g : points) {
            Point p = (Point) g.getObject();
            if (lastPoint != null) {
                LineString line = GEOMETRY_FACTORY.createLineString(
                        new Coordinate[]{lastPoint.getCoordinate(), p.getCoordinate()});
                System.out.println("Line " + lastPoint.getCoordinate() + ", " + p.getCoordinate());//test
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
     * two parts such that the smaller one is bigger or equal to 1), the biggest axis parallel-rectangle which
     * lies in ABCD and where the treasure could be located due to the information gained by the hint, is returned.
     * Otherwise the return value is null.
     *
     * @param A
     * @param B
     * @param C
     * @param D
     * @param hint
     * @param intersection_AD_hint
     * @param intersection_BC_hint
     * @return
     */
    private Point[] splitRectangleHorizontally(Point A, Point B, Point C, Point D, HalfPlaneHint hint,
                                               Point intersection_AD_hint, Point intersection_BC_hint) {
        if (intersection_AD_hint == null || intersection_BC_hint == null) {
            return null;
        }

        if (hint.getDirection() == up) {
            if ((intersection_AD_hint.getY() - D.getY()) >= 1) {
                Point newD = intersection_AD_hint;
                Point newC = intersection_BC_hint;
                return new Point[]{A, B, newC, newD};
            }
        }

        if (hint.getDirection() == down) {
            if ((A.getY() - intersection_AD_hint.getY()) >= 1) {
                Point newA = intersection_AD_hint;
                Point newB = intersection_BC_hint;
                return new Point[]{newA, newB, C, D};
            }
        }

        if (hint.pointsUpwards()) {
            if (intersection_AD_hint.distance(D) >= 1 && intersection_BC_hint.distance(C) >= 1) {
                if (intersection_AD_hint.distance(D) >= intersection_BC_hint.distance(C)) {
                    Point newD = JTSUtils.createPoint(D.getX(), intersection_BC_hint.getY());
                    Point newC = intersection_BC_hint;
                    return new Point[]{A, B, newC, newD};
                } else {
                    Point newC = JTSUtils.createPoint(C.getX(), intersection_AD_hint.getY());
                    Point newD = intersection_AD_hint;
                    return new Point[]{A, B, newC, newD};
                }
            }
        }
        if (hint.pointsDownwards()) {
            if (intersection_AD_hint.distance(A) >= intersection_BC_hint.distance(B)) {
                Point newA = JTSUtils.createPoint(A.getX(), intersection_BC_hint.getY());
                Point newB = intersection_BC_hint;
                return new Point[]{newA, newB, C, D};
            } else {
                Point newB = JTSUtils.createPoint(B.getX(), intersection_AD_hint.getY());
                Point newA = intersection_AD_hint;
                return new Point[]{newA, newB, C, D};
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
     * @param intersection_AB_hint
     * @param intersection_CD_hint
     * @return
     */
    private Point[] splitRectangleVertically(Point A, Point B, Point C, Point D, HalfPlaneHint hint,
                                             Point intersection_AB_hint, Point intersection_CD_hint) {
        // checks if the hint is good (i.e. if the hint divides one side of the rectangles in into two parts such that
        // the smaller one is bigger or equal to 1)
        if (intersection_AB_hint == null || intersection_CD_hint == null
                || (intersection_AB_hint.distance(A) < 1 || intersection_AB_hint.distance(B) < 1
                || intersection_CD_hint.distance(C) < 1 || intersection_CD_hint.distance(D) < 1)) {
            return null;
        }

        if (hint.getDirection() == left) {
            // determine which intersection-point has to be used to calculate the rectangle-points:
            if (intersection_AB_hint.distance(B) >= intersection_CD_hint.distance(C)) {
                Point newB = JTSUtils.createPoint(intersection_CD_hint.getX(), B.getY());
                Point newC = intersection_CD_hint;
                return new Point[]{A, newB, newC, D};
            } else {
                Point newC = JTSUtils.createPoint(intersection_AB_hint.getX(), C.getY());
                Point newB = intersection_AB_hint;
                return new Point[]{A, newB, newC, D};
            }
        }

        if (hint.getDirection() == right) {
            // determine which intersection-point has to be used to calculate the rectangle-points:
            if (intersection_AB_hint.distance(A) >= intersection_CD_hint.distance(D)) {
                Point newA = JTSUtils.createPoint(intersection_CD_hint.getX(), A.getY());
                Point newD = intersection_CD_hint;
                return new Point[]{newA, B, C, newD};
            } else {
                Point newD = JTSUtils.createPoint(intersection_AB_hint.getX(), D.getY());
                Point newA = intersection_AB_hint;
                return new Point[]{newA, B, C, newD};
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
