package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
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
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.BadHintSubroutine.lastHintBadSubroutine;
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
    int phase;  // equals j in the paper. In phase i, the algorithm checks a rectangle with a side length of 2^i
    //             centered at the initial position of the searcher
    Point start, // the initial position of the player
            A, B, C, D; // The points current rectangle where the treasure is to be searched.
    //                     ABCD lies always in the rectangle of the current phase.
    //                     The points are used like the points A,B,C and D in the paper.

    HalfPlaneHint lastBadHint; //only used when last hint was bad
    boolean lastHintWasBad = false;
    Point lastLocation;

    List<LineString> lastMove = null; // the lines of the move which was last calculated.

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
        double width = B.getX() - A.getX();
        double height = A.getY() - D.getY();
        if (width < 4 || height < 4) {
            return moveReturn(addState(incrementPhase(move)));
        }
        //now analyse the hint:
        if (lastHintWasBad) {
            return moveReturn(addState(lastHintBadSubroutine(this, hint, lastBadHint, move)));
        }

        LineSegment AB = new LineSegment(A.getCoordinate(), B.getCoordinate());
        LineSegment BC = new LineSegment(B.getCoordinate(), C.getCoordinate());
        LineSegment CD = new LineSegment(C.getCoordinate(), D.getCoordinate());
        LineSegment AD = new LineSegment(A.getCoordinate(), D.getCoordinate());

        LineSegment hintLine = new LineSegment(hint.getLeftPoint(),
                hint.getRightPoint());

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

        Point[] horizontalSplit = splitRectangleHorizontally(A, B, C, D, hint, intersection_AD_hint,
                intersection_BC_hint);
        if (horizontalSplit != null) {
            A = horizontalSplit[0];
            B = horizontalSplit[1];
            C = horizontalSplit[2];
            D = horizontalSplit[3];
            // "good" case (as defined in the paper)
            return moveReturn(addState(moveToCenterOfRectangle(A, B, C, D, move)));
        }
        Point[] verticalSplit = splitRectangleVertically(A, B, C, D, hint, intersection_AB_hint,
                intersection_CD_hint);
        if (verticalSplit != null) {
            A = verticalSplit[0];
            B = verticalSplit[1];
            C = verticalSplit[2];
            D = verticalSplit[3];
            // "good" case (as defined in the paper)
            return moveReturn(addState(moveToCenterOfRectangle(A, B, C, D, move)));
        }
        // when none of this cases takes place, the hint is bad (as defined in the paper). This gets handled here:
        Point destination = GEOMETRY_FACTORY.createPoint(twoStepsOrthogonal(hint, centerOfRectangle(A, B, C, D)));
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
        cur_coords[0] = A.getCoordinate();
        cur_coords[1] = B.getCoordinate();
        cur_coords[2] = C.getCoordinate();
        cur_coords[3] = D.getCoordinate();
        cur_coords[4] = A.getCoordinate();

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
                !doubleEqual(A.getX(), rect[0].getX()) && A.getX() < rect[0].getX() ||
                        !doubleEqual(A.getX(), rect[1].getX()) && A.getX() > rect[1].getX() ||
                        !doubleEqual(A.getY(), rect[0].getY()) && A.getY() > rect[0].getY() ||
                        !doubleEqual(A.getY(), rect[2].getY()) && A.getY() < rect[2].getY() ||


                        !doubleEqual(B.getX(), rect[0].getX()) && B.getX() < rect[0].getX() ||
                        !doubleEqual(B.getX(), rect[1].getX()) && B.getX() > rect[1].getX() ||
                        !doubleEqual(B.getY(), rect[0].getY()) && B.getY() > rect[0].getY() ||
                        !doubleEqual(B.getY(), rect[2].getY()) && B.getY() < rect[2].getY() ||

                        !doubleEqual(C.getX(), rect[0].getX()) && C.getX() < rect[0].getX() ||
                        !doubleEqual(C.getX(), rect[1].getX()) && C.getX() > rect[1].getX() ||
                        !doubleEqual(C.getY(), rect[0].getY()) && C.getY() > rect[0].getY() ||
                        !doubleEqual(C.getY(), rect[2].getY()) && C.getY() < rect[2].getY() ||

                        !doubleEqual(D.getX(), rect[0].getX()) && D.getX() < rect[0].getX() ||
                        !doubleEqual(D.getX(), rect[1].getX()) && D.getX() > rect[1].getX() ||
                        !doubleEqual(D.getY(), rect[0].getY()) && D.getY() > rect[0].getY() ||
                        !doubleEqual(D.getY(), rect[2].getY()) && D.getY() < rect[2].getY()
        ) {
            throw new AssertionError(
                    "phaseRect:\n" +
                            rect[0].toString() + "\n" +
                            rect[1].toString() + "\n" +
                            rect[2].toString() + "\n" +
                            rect[3].toString() + "\n" +
                            "ABCD:\n"
                            + Arrays.toString(A.getCoordinates()) + "\n" + Arrays.toString(B.getCoordinates()) + "\n"
                            + Arrays.toString(C.getCoordinates()) + "\n" + Arrays.toString(D.getCoordinates())
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
        if (lastMove != null) {
            for (LineString g : lastMove) {
                move.addAdditionalItem(
                        new GeometryItem(g, GeometryType.SEARCHER_MOVEMENT)
                );
            }
        }
        lastMove = new ArrayList<>();

        List<GeometryItem<Point>> points = move.getPoints();
        Point lastPoint = null;
        for (GeometryItem g : points) {
            Point p = (Point) g.getObject();
            if (lastPoint != null) {
                LineString line = GEOMETRY_FACTORY.createLineString(
                        new Coordinate[]{lastPoint.getCoordinate(), p.getCoordinate()});
                move.addAdditionalItem(
                        new GeometryItem(line, GeometryType.SEARCHER_LAST_MOVE)
                );
                lastMove.add(line);
            }
            lastPoint = p;
        }
        lastLocation = move.getEndPoint();
        return move;
    }

    private Movement moveReturnOld(Movement move) {
        List<GeometryItem<Point>> points = move.getPoints();
        Point lastPoint = null;
        for (GeometryItem g : points) {
            Point p = (Point) g.getObject();
            if (lastPoint != null) {
                LineString line = GEOMETRY_FACTORY.createLineString(
                        new Coordinate[]{lastPoint.getCoordinate(), p.getCoordinate()});
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
        Point oldA = A;
        Point oldB = B;
        Point oldC = C;
        Point oldD = D;
        setRectToPhase();
        rectangleScan(oldA, oldB, oldC, oldD, move);
        move.addWayPoint(centerOfRectangle(A, B, C, D));
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
        A = GEOMETRY_FACTORY.createPoint(rect[0]);
        B = GEOMETRY_FACTORY.createPoint(rect[1]);
        C = GEOMETRY_FACTORY.createPoint(rect[2]);
        D = GEOMETRY_FACTORY.createPoint(rect[3]);
    }
}
