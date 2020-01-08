package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.math.Vector2D;

import java.util.Arrays;
import java.util.List;

import static com.treasure.hunt.strategy.geom.GeometryType.CURRENT_PHASE;
import static com.treasure.hunt.strategy.geom.GeometryType.CURRENT_RECTANGLE;
import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.utils.JTSUtils.*;
import static org.locationtech.jts.algorithm.Angle.normalizePositive;

/**
 * This implements the strategy from the paper:
 * {@literal Treasure Hunt in the Plane with Angular Hints}
 *
 * @author Rank
 */

public class StrategyFromPaper implements Searcher<HalfPlaneHint> {
    int phase; //equals j in the paper. In phase i, the algorithm checks a rectangle with a side length of 2^i
    Point start,
            A, B, C, D;

    HalfPlaneHint lastBadHint; //only used when last hint was bad
    boolean lastHintWasBad = false;
    Point lastLocation;

    //just for testing
    private void printRect(Coordinate[] rect, HalfPlaneHint lastBadHint, HalfPlaneHint curHint) {
        System.out.println("A= (" + A.getX() + ", " + A.getY() + ")");
        System.out.println(" B= (" + B.getX() + ", " + B.getY() + ")");
        System.out.println(" C= (" + C.getX() + ", " + C.getY() + ")");
        System.out.println(" D= (" + D.getX() + ", " + D.getY() + ")");
        for (int i = 0; i < rect.length; i++)
            System.out.println("rect[" + i + "]= " + rect[i]);
        System.out.println("lastBadHint p1= " + lastBadHint.getAnglePointLeft() + "lastHint p2= " +
                lastBadHint.getAnglePointRight());
        System.out.println("curHint p1= " + curHint.getAnglePointLeft() + "curHint p2= " +
                curHint.getAnglePointRight());
    }
    //test end

    /**
     * {@inheritDoc}
     */
    public void init(Point startPosition) {
        start = startPosition;
        lastLocation = startPosition;
        phase = 1;
        setRectToPhase();
    }

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
                Coordinate[] line = new Coordinate[]{lastPoint.getCoordinate(), p.getCoordinate()};
                move.addAdditionalItem(
                        new GeometryItem(new LineString(new CoordinateArraySequence(line), GEOMETRY_FACTORY),
                                GeometryType.SEARCHER_MOVEMENT)
                );
            }
            lastPoint = p;
        }
        lastLocation = move.getEndPoint();
        return move;
    }

    @Override
    public Movement move() {
        Movement move = new Movement();
        move.addWayPoint(lastLocation);
        return addState(incrementPhase(move));
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
        if (lastHintWasBad)
            return moveReturn(lastHintBadSubroutine(hint, move));

        LineSegment AB = new LineSegment(A.getCoordinate(), B.getCoordinate());
        LineSegment BC = new LineSegment(B.getCoordinate(), C.getCoordinate());
        LineSegment CD = new LineSegment(C.getCoordinate(), D.getCoordinate());
        LineSegment AD = new LineSegment(A.getCoordinate(), D.getCoordinate());

        LineSegment hintLine = new LineSegment(hint.getAnglePointLeft(),
                hint.getAnglePointRight());

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
            return moveReturn(addState(moveToCenterOfRectangle(A, B, C, D, move)));
        }
        Point[] verticalSplit = splitRectangleVertically(A, B, C, D, hint, intersection_AB_hint,
                intersection_CD_hint);
        if (verticalSplit != null) {
            A = verticalSplit[0];
            B = verticalSplit[1];
            C = verticalSplit[2];
            D = verticalSplit[3];
            return moveReturn(addState(moveToCenterOfRectangle(A, B, C, D, move)));
        }
        return moveReturn(addState(badHintSubroutine(hint, move)));
    }

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

    private Movement badHintSubroutine(HalfPlaneHint hint, Movement move) {
        //return moveToCenterOfRectangle(A, B, C, D); //testing

        Point direction = GEOMETRY_FACTORY.createPoint(twoStepsOrthogonal(hint, centerOfRectangle(A, B, C, D)));
        move.addWayPoint(direction);
        lastHintWasBad = true;
        lastBadHint = hint;
        return move;
    }

    private Coordinate twoStepsOrthogonal(HalfPlaneHint hint, Point P) {
        return twoStepsOrthogonal(hint, P.getCoordinate());
    }

    private Coordinate twoStepsOrthogonal(HalfPlaneHint hint, Coordinate cur_pos) {
        Vector2D hintVector = new Vector2D(hint.getAnglePointLeft(),
                hint.getAnglePointRight());

        hintVector = hintVector.divide(hintVector.length() / 2);
        hintVector = hintVector.rotateByQuarterCircle(1);

        /*switch (hint.getDirection()) {
            case up:
                return new Coordinate(cur_pos.getX(), cur_pos.getY() + 2);
            case down:
                return new Coordinate(cur_pos.getX(), cur_pos.getY() - 2);
            case left:
                hintVector = hintVector.rotateByQuarterCircle(1);
            case right:
                hintVector = hintVector.rotateByQuarterCircle(3);
        }*/
        return new Coordinate(cur_pos.getX() + hintVector.getX(), cur_pos.getY() + hintVector.getY());
    }

    private Point centerOfRectangle(Point P1, Point P2, Point P3, Point P4) {
        LineString line13 = JTSUtils.createLineString(P1, P3);
        return line13.getCentroid();
    }

    private Coordinate centerOfRectangle(Coordinate[] rect) {
        LineSegment lineAC = new LineSegment(rect[0], rect[2]);
        return lineAC.midPoint();
    }

    private Movement moveToCenterOfRectangle(Point P1, Point P2, Point P3, Point P4, Movement move) {
        move.addWayPoint(centerOfRectangle(P1, P2, P3, P4));
        return move;
    }

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

    // returns the rectangle of the current phase, just by using the current phase index (j in the paper, "phase" in
    // the implementation)
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

    // Sets the rectangle ABCD to the rectangle of the current phase.
    private void setRectToPhase() {
        Coordinate[] rect = phaseRectangle();
        A = GEOMETRY_FACTORY.createPoint(rect[0]);
        B = GEOMETRY_FACTORY.createPoint(rect[1]);
        C = GEOMETRY_FACTORY.createPoint(rect[2]);
        D = GEOMETRY_FACTORY.createPoint(rect[3]);
    }


    private Movement rectangleScan(Point A, Point B, Point C, Point D, Movement move) {
        return rectangleScan(A.getCoordinate(), B.getCoordinate(), C.getCoordinate(), D.getCoordinate(), move);
    }

    private Movement rectangleScan(Coordinate A, Coordinate B, Coordinate C, Coordinate D, Movement move) {
        int k = (int) A.distance(B);
        Point[] a = new Point[k + 1];
        Point[] b = new Point[k + 1];

        { //create a_i on line segment AB
            double xDist = B.getX() - A.getX();
            double yDist = B.getY() - A.getY();
            for (int i = 0; i <= k; i++) {
                a[i] = JTSUtils.createPoint(A.getX() + xDist * ((double) i / (double) k), A.getY() + yDist *
                        ((double) i / (double) k));
            }
        }
        { //create b_i on line segment DC
            double xDist = C.getX() - D.getX();
            double yDist = C.getY() - D.getY();
            //for (int i = 0; i <= k; i++) {
            for (int i = 0; i <= k; i++) {
                b[i] = JTSUtils.createPoint(D.getX() + xDist * ((double) i / (double) k), D.getY() + yDist *
                        ((double) i / (double) k));
            }
        }

        if (k % 2 == 1) //code like in paper
        {
            //for (int i = 0; i <= k - 1; k += 2) {
            for (int i = 0; i <= k - 1; i += 2) {
                move.addWayPoint(a[i]);
                move.addWayPoint(b[i]);
                move.addWayPoint(b[i + 1]);
                move.addWayPoint(a[i + 1]);
            }
        } else {
            //for (int i = 0; i <= k - 2; k += 2) {
            for (int i = 0; i <= k - 2; i += 2) {
                move.addWayPoint(a[i]);
                move.addWayPoint(b[i]);
                move.addWayPoint(b[i + 1]);
                move.addWayPoint(a[i + 1]);
            }
            move.addWayPoint(a[k]);
            move.addWayPoint(b[k]);
        }
        //move.addWayPoint(a); // go to a
        return move;
    }

    /**
     * If the last hint was bad, this function can be called and lastBadHint has to be set accordingly.
     * The function equals the "else"-part of the first if-condition in Algorithm 3 (Function ReduceRectangle(R))
     * in the paper.
     * Variable names are equivalent to the paper, but since a', d', etc. is not a valid variable name in Java,
     * _apos is used in such cases (apos for apostrophe), e.g. a_apos in this implementation equates to a'
     * in the paper. _doubleApos signals a double apostrophe.
     * At, Bt, Ct and Dt in this implementation equate to A, B, C and D in the paper, since the
     * not by phi transformed variables of the current rectangle R are also stored with A, B, C and D.
     * The t signals the transformed state of these variables.
     * hintT equates to the hint (L1', x1')
     *
     * @param curHint
     * @return The move to scan various areas so that A,B,C and D can be updated to a smaller rectangle (or the treasure
     * is found)
     */
    private Movement lastHintBadSubroutine(HalfPlaneHint curHint, Movement move) {
        Coordinate[] rect = new Coordinate[]{A.getCoordinate(), B.getCoordinate(), C.getCoordinate(), D.getCoordinate()};
        try {
            int basicTrans = getBasicTransformation(rect, lastBadHint); // basic transformation
            Coordinate[] transformedRect = phiRectangle(basicTrans, rect);
            Coordinate At = transformedRect[0];
            Coordinate Bt = transformedRect[1];
            Coordinate Ct = transformedRect[2];
            Coordinate Dt = transformedRect[3];
            HalfPlaneHint hintT = phiHint(basicTrans, rect, lastBadHint);

            Coordinate p = centerOfRectangle(transformedRect);
            Coordinate p_apos = twoStepsOrthogonal(hintT, p);
            double p_to_p_apos_x = p_apos.getX() - p.getX(); // the x coordinate of the vector from p to p_apos
            double p_to_p_apos_y = p_apos.getY() - p.getY(); // the y coordinate of the vector from p to p_apos

            LineSegment ABt = new LineSegment(At, Bt);
            LineSegment ADt = new LineSegment(At, Dt);
            LineSegment BCt = new LineSegment(Bt, Ct);
            LineSegment CDt = new LineSegment(Ct, Dt);
            LineSegment L1_apos = new LineSegment(hintT.getAnglePointLeft(),
                    hintT.getAnglePointRight());
            LineSegment L1_doubleApos = new LineSegment(
                    hintT.getAnglePointLeft().getX() + p_to_p_apos_x,
                    hintT.getAnglePointLeft().getY() + p_to_p_apos_y,
                    hintT.getAnglePointRight().getX() + p_to_p_apos_x,
                    hintT.getAnglePointRight().getY() + p_to_p_apos_y
            );

            Coordinate a = lineWayIntersection(L1_apos, ADt);
            Coordinate d = lineWayIntersection(L1_apos, BCt);
            Coordinate e = null;
            if (d != null)
                e = new Coordinate(Dt.getX(), d.getY());
            Coordinate d_apos = null;
            if (d != null)
                d_apos = twoStepsOrthogonal(lastBadHint, d);

            System.out.println("L1_doubleApos = " + L1_doubleApos); //testing
            System.out.println("ABt = " + ABt); //testing
            Coordinate f = lineWayIntersection(L1_doubleApos, ABt);
            System.out.println("f = " + f); //testing
            Coordinate j = lineWayIntersection(L1_doubleApos, BCt);

            Coordinate j_apos = null;
            if (j != null)
                j_apos = new Coordinate(Dt.getX(), j.getY());
            Coordinate t = null;
            //if (f != null)
            t = new Coordinate(f.getX(), Dt.getY());

            Coordinate m = new Coordinate(At.getX(), p.getY());
            Coordinate m_apos = new Coordinate(At.getX(), p_apos.getY());
            Coordinate k = new Coordinate(Bt.getX(), p.getY());
            Coordinate k_apos = new Coordinate(Bt.getX(), p_apos.getY());

            Coordinate g = new Coordinate(p.getX(), At.getY());
            Coordinate g_apos = new Coordinate(p_apos.getX(), At.getY());
            Coordinate h = new Coordinate(p.getX(), Dt.getY());
            Coordinate h_apos = new Coordinate(p_apos.getX(), Dt.getY());

            Coordinate s, s_apos;

            LineSegment A_s_apos = new LineSegment(At.getX(), At.getY(),
                    At.getX() + p_to_p_apos_x, At.getY() + p_to_p_apos_y);
            // the line from A to s gets constructed by using the line from p to p' (p_apos)
            s = new Coordinate(L1_apos.lineIntersection(A_s_apos));
            s_apos = new Coordinate(L1_doubleApos.lineIntersection(A_s_apos));

            HalfPlaneHint curHintT = phiHint(basicTrans, rect, curHint);

            //testing:
            System.out.println("curHintTransformed (" + curHintT.getAnglePointLeft() + ", " + curHintT.getAnglePointRight() + ")");
            System.out.println("transformedHint (" + hintT.getAnglePointLeft() + ", " + hintT.getAnglePointRight() + ")");
            System.out.println("transformedRect " + Arrays.toString(transformedRect)); // testing

            HalfPlaneHint.Direction x2_apos = curHintT.getDirection();
            LineSegment L2_apos = new LineSegment(curHintT.getAnglePointLeft(),
                    curHintT.getAnglePointRight());

            // here begins line 24 of the ReduceRectangle routine from the paper:
            Coordinate[] newRectangle = null;

            LineSegment pp_apos = new LineSegment(p, p_apos);
            if (x2_apos == right &&
                    lineBetweenClockwise(L2_apos, L1_doubleApos, pp_apos)
            ) {
                System.out.println("--------------------------------------------------erster fall"); //testing
                /*
                System.out.println("f, Bt, Ct, t = \n" +
                        f + '\n' + Bt + "\n" + Ct + "\n" + t + "\n"); // testing
                System.out.println("phiOtherRectangleInverse(basicTrans, rect, (f, Bt, Ct, t) = \n" +
                        Arrays.toString(phiOtherRectangleInverse(basicTrans, rect, new Coordinate[]{f, Bt, Ct, t})));
                //testing
                */
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{f, Bt, Ct, t});
            }

            LineSegment m_apos_k_apos = new LineSegment(m_apos, k_apos);
            if (x2_apos == right &&
                    lineBetweenClockwise(L2_apos, pp_apos, m_apos_k_apos)
            ) {
                System.out.println("--------------------------------------------------zweiter fall"); //testing
                move = rectangleScanPhiReverse(basicTrans, rect, m_apos, k_apos, k, m, move);
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{g, Bt, Ct, h});
            }
            if ((x2_apos == left || x2_apos == down) &&
                    lineBetweenClockwise(L2_apos, m_apos_k_apos, L1_doubleApos)
            ) {
                System.out.println("--------------------------------------------------dritter fall"); //testing

                // rectangleScan(phi_reverse(k, (s, s', d', d))
                move = rectangleScanPhiReverse(basicTrans, rect, s, s_apos, d_apos, d, move);
                // rectangleScan(phi_reverse(k, (m', k', k, m))
                move = rectangleScanPhiReverse(basicTrans, rect, m_apos, k_apos, k, m, move);
                // newRectangle := pkCh
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{p, k, Ct, h});
            }
            LineSegment h_apos_g_apos = new LineSegment(h_apos, g_apos);
            if (x2_apos == left &&
                    lineBetweenClockwise(L2_apos, L1_doubleApos, h_apos_g_apos)
            ) {
                System.out.println("--------------------------------------------------vierter fall"); //testing

                // rectangleScan(phi_reverse(k, (s, s', d', d))
                move = rectangleScanPhiReverse(basicTrans, rect, s, s_apos, d_apos, d, move);
                // rectangleScan(phi_reverse(k, (g, g', h', h))
                // newRectangle := Agpm
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{At, g, p, m});
            }

            LineSegment p_apos_k = new LineSegment(p_apos, k);

            if ((x2_apos == left &&
                    lineBetweenClockwise(L2_apos, h_apos_g_apos, pp_apos)) ||
                    (x2_apos == left &&
                            lineBetweenClockwise(L2_apos, pp_apos, m_apos_k_apos)) ||
                    ((x2_apos == up || x2_apos == right) &&
                            lineBetweenClockwise(L2_apos, m_apos_k_apos, p_apos_k)
                    )
            ) {
                System.out.println("--------------------------------------------------fuenfter fall"); //testing

                // rectangleScan(phireverse(k, (g, g', h', h))
                move = rectangleScanPhiReverse(basicTrans, rect, g, g_apos, h_apos, h, move);
                // newRectangle := ABkm
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{At, Bt, k, m});
            }
            if (x2_apos == right &&
                    lineBetweenClockwise(L2_apos, p_apos_k, L1_doubleApos)
            ) {
                System.out.println("--------------------------------------------------sechster fall"); //testing
                // newRectangle := ABjj'
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{At, Bt, j, j_apos});
            }

            A = GEOMETRY_FACTORY.createPoint(newRectangle[0]);
            B = GEOMETRY_FACTORY.createPoint(newRectangle[1]);
            C = GEOMETRY_FACTORY.createPoint(newRectangle[2]);
            D = GEOMETRY_FACTORY.createPoint(newRectangle[3]);
            lastHintWasBad = false;
            return moveToCenterOfRectangle(A, B, C, D, move);
        } catch (Exception ee) {
            printRect(rect, lastBadHint, curHint);
            throw ee;
        }

    }

    /**
     * Returns true if line is clockwise between between1 (included) and between2 (excluded).
     * Its taken for granted that line between1 and between2 meet in one Point.
     *
     * @param line
     * @param between1
     * @param between2
     * @return if line is clockwise between between1 (included) and between2 (excluded)
     */
    private boolean lineBetweenClockwise(LineSegment line, LineSegment between1, LineSegment between2) {
        LineSegment lineReverse = new LineSegment(line.p1, line.p0);
        LineSegment between2reverse = new LineSegment(between2.p1, between2.p0);
        double angleBetween1 = between1.angle();
        double maxAngleLineBetween1 = Math.max(normalizePositive(line.angle() - angleBetween1), normalizePositive(lineReverse.angle() - angleBetween1));
        double maxAngleBetween2and1 = Math.max(normalizePositive(between2.angle() - angleBetween1), normalizePositive(between2reverse.angle() - angleBetween1));
        if (maxAngleLineBetween1 == 0)
            return true;
        return maxAngleBetween2and1 < maxAngleLineBetween1;
    }

    private void assertRectangle(Coordinate[] rect) {
        if (rect.length != 4)
            throw new IllegalArgumentException("The rectangle has " + rect.length + " points. It should have 4.");
    }

    /**
     * Returns the result of rho, defined by rectangle rect, applied on hint.
     *
     * @param rect defines rho
     * @param hint used as input in rho
     * @return
     */
    private HalfPlaneHint rhoHint(Coordinate[] rect, HalfPlaneHint hint) {
        assertRectangle(rect);
        LineSegment AB = new LineSegment(rect[0], rect[1]);
        LineSegment CD = new LineSegment(rect[2], rect[3]);
        Coordinate centerAB = AB.midPoint();
        Coordinate centerCD = CD.midPoint();
        AffineTransformation reflection = AffineTransformation.reflectionInstance(centerAB.getX(), centerAB.getY(),
                centerCD.getX(), centerCD.getY());
        Coordinate newAPLeft = new Coordinate();
        Coordinate newAPRight = new Coordinate();
        reflection.transform(hint.getAnglePointRight(), newAPLeft);
        reflection.transform(hint.getAnglePointLeft(), newAPRight);
        HalfPlaneHint newHint = new HalfPlaneHint(newAPLeft, newAPRight);
        return newHint;
    }

    /**
     * Returns the result of rho, defined by rectangle rect, applied on P.
     *
     * @param rect
     * @param P
     * @return
     */
    private Coordinate rhoPoint(Coordinate[] rect, Coordinate P) {
        assertRectangle(rect);
        LineSegment AB = new LineSegment(rect[0], rect[1]);
        LineSegment CD = new LineSegment(rect[2], rect[3]);
        Coordinate centerAB = AB.midPoint();
        Coordinate centerCD = CD.midPoint();
        AffineTransformation reflection = AffineTransformation.reflectionInstance(centerAB.getX(), centerAB.getY(),
                centerCD.getX(), centerCD.getY());

        Coordinate transformedP = new Coordinate();
        return reflection.transform(P, transformedP);
    }

    /**
     * sigma is defined like in the paper, P is the Point which is to be transformed and r is the middle point of the
     * rectangle, i is the index.
     *
     * @param i the index of sigma
     * @param r the center of a rectangle which is used as the point to rotate around
     * @param P the Point which is to be transformed
     * @return
     */
    private Coordinate sigmaPoint(int i, Coordinate r, Coordinate P) {
        //AffineTransformation rot_i = AffineTransformation.rotationInstance(Math.PI * i / 2, r.getX(), r.getY());
        AffineTransformation rot_i;
        switch (i) {
            case 0:
                rot_i = new AffineTransformation(new double[]{1, 0, 0, 0, 1, 0});
                break;
            case 1:
                rot_i = new AffineTransformation(new double[]{0, -1, 0, 1, 0, 0});
                break;
            case 2:
                rot_i = new AffineTransformation(new double[]{-1, 0, 0, 0, -1, 0});
                break;
            case 3:
                rot_i = new AffineTransformation(new double[]{0, 1, 0, -1, 0, 0});
                break;
            default:
                throw new IllegalArgumentException("i should be in [0,3] but equals " + i);
        }
        Coordinate ret = new Coordinate(P.x - r.x, P.y - r.y);
        rot_i.transform(ret, ret);
        ret.x = ret.x + r.x;
        ret.y = ret.y + r.y;
        return ret;

    }

    private Coordinate sigmaPointReverse(int i, Coordinate r, Coordinate P) {
        return sigmaPoint(3 - i, r, P);
    }

    /**
     * Returns the result of sigma, defined by index i and rectangle rect, applied on the points in rect.
     *
     * @param i
     * @param rect
     * @return
     */
    private Coordinate[] sigmaRectangle(int i, Coordinate[] rect) {
        assertRectangle(rect);
        Coordinate r = centerOfRectangle(rect);

        AffineTransformation rotHalfPi = new AffineTransformation(new double[]{0, -1, 0, 1, 0, 0});

        if (i == 0 || i == 2) {
            return rect;
        }
        if (i == 1 || i == 3) {
            //rotate rectangle by pi/2
            Coordinate[] transformed = new Coordinate[]{new Coordinate(), new Coordinate(),
                    new Coordinate(), new Coordinate()};
            for (int j = 0; j <= 3; j++) {
                transformed[j].x = rect[j].x - r.x;
                transformed[j].y = rect[j].y - r.y;
            }

            Coordinate transformed0old = transformed[0].copy();
            rotHalfPi.transform(transformed[1], transformed[0]);
            rotHalfPi.transform(transformed[2], transformed[1]);
            rotHalfPi.transform(transformed[3], transformed[2]);
            rotHalfPi.transform(transformed0old, transformed[3]);

            for (int j = 0; j <= 3; j++) {
                transformed[j].x = transformed[j].x + r.x;
                transformed[j].y = transformed[j].y + r.y;
            }
            return transformed;
        }
        throw new IllegalArgumentException("i should be in [0,3] but is equal to " + i);
    }

    private Coordinate[] sigmaRectangleReverse(int i, Coordinate[] rect) {
        return sigmaRectangle(i, rect);
    }

    /**
     * Returns the result of phi, defined by rect, with index i, applied on rect.
     *
     * @param i
     * @param rect
     * @return
     */
    private Coordinate[] phiRectangle(int i, Coordinate[] rect) {
        assertRectangle(rect);
        if (i < 0 || i > 7)
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        return sigmaRectangle(i % 4, rect);
    }


    private Coordinate phiPoint(int i, Coordinate[] rect, Coordinate P) {
        assertRectangle(rect);
        if (i < 0 || i > 7)
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        Coordinate r = centerOfRectangle(rect);
        if (i < 4)
            return sigmaPoint(i, r, P);
        return sigmaPoint(i, r, rhoPoint(rect, P));
    }

    /**
     * Returns the result of phi, defined by rect, with index i, applied on hint.
     *
     * @param i    the index of phi
     * @param rect the rectangle which defines phi
     * @param hint the hint that is used as input in phi
     * @return
     */
    private HalfPlaneHint phiHint(int i, Coordinate[] rect, HalfPlaneHint hint) {
        assertRectangle(rect);
        if (i < 0 || i > 7)
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        Coordinate r = centerOfRectangle(rect);
        HalfPlaneHint transformedHint = new HalfPlaneHint(
                sigmaPoint(i % 4, r, hint.getAnglePointLeft()),
                sigmaPoint(i % 4, r, hint.getAnglePointRight())
        );
        if (i < 4)
            return transformedHint;
        return rhoHint(rect, transformedHint);
    }

    /**
     * Calculates the inverse phi operation defined by the rectangle rect and applies it on P.
     *
     * @param i    the index of phi
     * @param rect the rectangle which defines phi (and therefore also the inverse of phi)
     * @param P
     * @return
     */
    private Coordinate phiPointInverse(int i, Coordinate[] rect, Coordinate P) {
        assertRectangle(rect);
        if (i < 0 || i > 7)
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        Coordinate r = centerOfRectangle(rect);
        if (i < 4) {
            return sigmaPointReverse(i, r, P);
        }
        return sigmaPointReverse(i - 4, r, rhoPoint(rect, P));
    }

    private Movement rectangleScanPhiReverse(int basicTrans, Coordinate[] phiRect,
                                             Coordinate A, Coordinate B, Coordinate C, Coordinate D, Movement move) {
        return rectangleScan(
                phiPointInverse(basicTrans, phiRect, A),
                phiPointInverse(basicTrans, phiRect, B),
                phiPointInverse(basicTrans, phiRect, C),
                phiPointInverse(basicTrans, phiRect, D),
                move
        );
    }

    /**
     * Returns the basic transformation for a rectangle-hint-pair, of which the definition can be found in the paper.
     * (Page 8, below Proposition 3.2)
     *
     * @param rect
     * @param hint
     * @return
     */
    private int getBasicTransformation(Coordinate[] rect, HalfPlaneHint hint) {
        for (int i = 0; i <= 7; i++) {
            Coordinate[] testRect = phiRectangle(i, rect);
            HalfPlaneHint testHint = phiHint(i, rect, hint);
            LineSegment hintLine = new LineSegment(testHint.getAnglePointLeft(),
                    testHint.getAnglePointRight());
            LineSegment testAD = new LineSegment(testRect[0], testRect[3]);
            Coordinate AD_hint = lineWayIntersection(hintLine, testAD);
            if (testHint.getDirection() == up)
                return i;
            if (testHint.getDirection() == right &&
                    testHint.getUpperHintPoint().getX() < testHint.getLowerHintPoint().getX() &&
                    AD_hint != null)
                return i;
        }
        System.out.println("rect: " + Arrays.toString(rect) + " anglepoints: "
                + hint.getAnglePointRight() + hint.getAnglePointLeft());
        throw new IllegalArgumentException("Somehow there was no basic transformation found for this " +
                "rectangle and hint. This is impossible.");
    }

    /**
     * Inverts the by rect defined phi , but calculates and returns the inverse points of the points in toTransform.
     *
     * @param i           the index of phi
     * @param rect        the rectangle which defines phi (and therefore also the inverse of phi)
     * @param toTransform
     * @return
     */
    private Coordinate[] phiOtherRectangleInverse(int i, Coordinate[] rect, Coordinate[] toTransform) {
        //TODO: build arrangeRectangle in this method
        assertRectangle(rect);
        assertRectangle(toTransform);
        Coordinate[] ret = new Coordinate[]{
                phiPointInverse(i, rect, toTransform[0]),
                phiPointInverse(i, rect, toTransform[1]),
                phiPointInverse(i, rect, toTransform[2]),
                phiPointInverse(i, rect, toTransform[3])
        };
        return arrangeRectangle(ret);
    }

    /**
     * Coordinate of rect are rounded in the precision model grid and reordered so tha rect[0] is the upper left point,
     * rect[1] is the upper right point, rect[2] is the bottom right point and rect[3] is the bottom left point.
     * If the rectangle is not parallel to the x and y axis, an error is thrown.
     *
     * @param rect
     * @return
     */
    private Coordinate[] arrangeRectangle(Coordinate[] rect) {
        assertRectangle(rect);

        Coordinate A = null, B = null, C = null, D = null;
        double max_x = -Double.MAX_VALUE;
        double max_y = -Double.MAX_VALUE;
        double min_x = Double.MAX_VALUE;
        double min_y = Double.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            double x = rect[i].getX();
            double y = rect[i].getY();

            max_x = Math.max(max_x, x);
            max_y = Math.max(max_y, y);
            min_x = Math.min(min_x, x);
            min_y = Math.min(min_y, y);


            if (doubleEqual(min_x, x) && doubleEqual(max_y, y))
                A = rect[i];
            if (doubleEqual(max_x, x) && doubleEqual(max_y, y))
                B = rect[i];
            if (doubleEqual(max_x, x) && doubleEqual(min_y, y))
                C = rect[i];
            if (doubleEqual(min_x, x) && doubleEqual(min_y, y))
                D = rect[i];
        }
        if (A == null || B == null || C == null || D == null)
            throw new IllegalArgumentException("rect is malformed. It equals " + rect[0] + rect[1] + rect[2] + rect[3]);

        if (A.equals2D(B) || A.equals2D(C) || A.equals2D(D) || B.equals2D(C) || B.equals2D(D) || C.equals2D(D)) {
            throw new IllegalArgumentException("rect is malformed. It equals " + rect[0] + rect[1] + rect[2] + rect[3]);
        }
        //if (A.x != C.x || A.y != B.y || B.x != D.x || C.y != D.y) {
        if (!doubleEqual(A.x, D.x) || !doubleEqual(A.y, B.y) || !doubleEqual(B.x, C.x) || !doubleEqual(C.y, D.y)) {
            throw new IllegalArgumentException("rect is not parallel to x an y axis:" +
                    "\nrect[0] = " + rect[0] +
                    "\nrect[1] = " + rect[1] +
                    "\nrect[2] = " + rect[2] +
                    "\nrect[3] = " + rect[3] +
                    "\nA = " + A +
                    "\nB = " + B +
                    "\nC = " + C +
                    "\nD = " + D);

        }
        Coordinate[] rectRes = new Coordinate[4];
        rectRes[0] = A;
        rectRes[1] = B;
        rectRes[2] = C;
        rectRes[3] = D;

        return rectRes;
    }

    public static class TestThisClass {
        StrategyFromPaper strategy;

        public TestThisClass(StrategyFromPaper strategy) {
            this.strategy = strategy;
        }

        private void testRectHint(Coordinate[] rect, HalfPlaneHint hint, int basicTrans) {
            int testBasicTrans = strategy.getBasicTransformation(rect, hint);
            if (basicTrans != testBasicTrans) {
                throw new IllegalArgumentException("The basic transformation should equal " + basicTrans +
                        " but equals " + testBasicTrans);
            }
        }

        private void testLastHintBadSubroutine(StrategyFromPaper strategy, Coordinate[] rect, HalfPlaneHint lastBadHint,
                                               HalfPlaneHint curHint) {
            strategy.A = GEOMETRY_FACTORY.createPoint(rect[0]);
            strategy.B = GEOMETRY_FACTORY.createPoint(rect[1]);
            strategy.C = GEOMETRY_FACTORY.createPoint(rect[2]);
            strategy.D = GEOMETRY_FACTORY.createPoint(rect[3]);
            strategy.lastBadHint = lastBadHint;
            strategy.lastHintBadSubroutine(curHint, new Movement());
        }

        public void testBadCases() {

            Coordinate[] rect = new Coordinate[]{new Coordinate(-4, 4), new Coordinate(4, 4),
                    new Coordinate(4, -4), new Coordinate(-4, -4)};
            HalfPlaneHint lastBadHint = new HalfPlaneHint(new Coordinate(0, 0),
                    new Coordinate(0.7377637010688854, -0.675059050294965));
            HalfPlaneHint curHint = new HalfPlaneHint(new Coordinate(1.3501181005899303, 1.4755274021377711),
                    new Coordinate(2.3366624680024213, 1.3120336373432429));
            //should be case five
            testRectHint(rect, lastBadHint, 0);
            strategy.A = createPoint(-2, 2);
            strategy.B = createPoint(2, 2);
            strategy.C = createPoint(2, -2);
            strategy.D = createPoint(-2, -2);
            strategy.lastBadHint = lastBadHint;
            strategy.lastHintBadSubroutine(curHint, new Movement());

            HalfPlaneHint hint = new HalfPlaneHint(new Coordinate(0, 0),
                    new Coordinate(0.6209474701786085, 0.7838521794820666));
            testRectHint(rect, hint, 3);
            rect = new Coordinate[]{new Coordinate(-2, 2), new Coordinate(2, 2),
                    new Coordinate(2, -2), new Coordinate(-2, -2)};
            hint = new HalfPlaneHint(new Coordinate(0, 0),
                    new Coordinate(0.7416025214414383, 0.6708395487683333));
            testRectHint(rect, hint, 4);

            //badCase0
            rect = new Coordinate[]{
                    new Coordinate(-2.159168821737699, 8.0),
                    new Coordinate(8.0, 8.0),
                    new Coordinate(8.0, -3.999532170942503),
                    new Coordinate(-2.159168821737699, -3.999532170942503)
            };
            lastBadHint = new HalfPlaneHint(new Coordinate(2.9204156, 2.0002339),
                    new Coordinate(3.5662858179937924, 2.7636811224775273));
            curHint = new HalfPlaneHint(new Coordinate(1.3935211550449453, 3.291974335987585),
                    new Coordinate(2.3662835676900604, 3.060169917253051));
            testRectHint(rect, lastBadHint, 3);
            testLastHintBadSubroutine(strategy, rect, lastBadHint, curHint);

            //badCase0 transformed so that basicTrans is 0
            rect = new Coordinate[]{
                    new Coordinate(-3.999532170942503, 2.3662835676900604),
                    new Coordinate(8.0, 2.3662835676900604),
                    new Coordinate(8.0, -8.0),
                    new Coordinate(-3.999532170942503, -8.0)
            };
            lastBadHint = new HalfPlaneHint(new Coordinate(2.9204156, 2.0002339),
                    new Coordinate(3.291974335987585, -1.3935211550449453));
            curHint = new HalfPlaneHint(new Coordinate(2.7636811224775273, -3.5662858179937924),
                    new Coordinate(3.060169917253051, -2.3662835676900604));
            //testRectHint(rect, lastBadHint, 0); //TODO evtl diesen test rauswerfen
            //testLastHintBadSubroutine(strategy, rect, lastBadHint, curHint);
        }

        public void testPhiRectangle() {
            Coordinate[] rect = new Coordinate[]{new Coordinate(-4, 4), new Coordinate(4, 4),
                    new Coordinate(4, -4), new Coordinate(-4, -4)};
            Coordinate[] testRect = strategy.phiRectangle(3, rect);
            if (!doubleEqual(testRect[0].x, -4) || !doubleEqual(testRect[0].y, 4) ||
                    !doubleEqual(testRect[1].x, 4) || !doubleEqual(testRect[1].y, 4) ||
                    !doubleEqual(testRect[2].x, 4) || !doubleEqual(testRect[2].y, -4) ||
                    !doubleEqual(testRect[3].x, -4) || !doubleEqual(testRect[3].y, -4)) {
                throw new IllegalArgumentException(Arrays.toString(testRect));
            }
        }

        public void testPhiHint() {
            Coordinate[] rect = new Coordinate[]{new Coordinate(-4, 4), new Coordinate(4, 4),
                    new Coordinate(4, -4), new Coordinate(-4, -4)};
            HalfPlaneHint hint = new HalfPlaneHint(new Coordinate(0, 0),
                    new Coordinate(0.6209474701786085, 0.7838521794820666));
            HalfPlaneHint testHint = strategy.phiHint(3, rect, hint);
            if (!doubleEqual(testHint.getAnglePointRight().getX(), 0.7838521794820666) ||
                    !doubleEqual(testHint.getAnglePointRight().getY(), -0.6209474701786085)) {
                throw new IllegalArgumentException("right angle point is " + testHint.getAnglePointRight() +
                        " and should equal (0.7838521794820666, -0.6209474701786085)");
            }
            if (!doubleEqual(testHint.getAnglePointLeft().getX(), 0) ||
                    !doubleEqual(testHint.getAnglePointLeft().getY(), 0)) {
                throw new IllegalArgumentException("left angle point is " + testHint.getAnglePointLeft() +
                        " and should equal (0.0, 0.0)");
            }
        }
    }
}

