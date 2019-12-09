package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.math.Vector2D;

import static com.treasure.hunt.strategy.geom.GeometryType.CURRENT_PHASE;
import static com.treasure.hunt.strategy.geom.GeometryType.CURRENT_RECTANGLE;
import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.utils.JTSUtils.GEOMETRY_FACTORY;
import static com.treasure.hunt.utils.JTSUtils.lineWayIntersection;
import static org.locationtech.jts.algorithm.Angle.normalizePositive;

public class StrategyFromPaper implements Searcher<HalfPlaneHint> {
    int phase; //equals j in the paper. In phase i, the algorithm checks a rectangle with a side length of 2^i
    Point start,
            A, B, C, D;

    HalfPlaneHint lastBadHint; //only used when last hint was bad
    boolean lastHintWasBad = false;

    @Override
    public void init(Point startPosition) {
        start = startPosition;
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

    @Override
    public Movement move() {
        return addState(incrementPhase());
    }

    @Override
    public Movement move(HalfPlaneHint hint) {
        double width = B.getX() - A.getX();
        double height = A.getY() - D.getY();
        if (width < 4 || height < 4) {
            return addState(incrementPhase());
        }
        //now analyse the hint:
        if (lastHintWasBad)
            return lastHintBadSubroutine(hint);

        LineSegment AB = new LineSegment(A.getCoordinate(), B.getCoordinate());
        LineSegment BC = new LineSegment(B.getCoordinate(), C.getCoordinate());
        LineSegment CD = new LineSegment(C.getCoordinate(), D.getCoordinate());
        LineSegment AD = new LineSegment(A.getCoordinate(), D.getCoordinate());

        LineSegment hintLine = new LineSegment(hint.getAnglePointLeft().getCoordinate(),
                hint.getAnglePointRight().getCoordinate());

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
            return addState(moveToCenterOfRectangle(A, B, C, D));
        }
        Point[] verticalSplit = splitRectangleVertically(A, B, C, D, hint, intersection_AB_hint,
                intersection_CD_hint);
        if (verticalSplit != null) {
            A = verticalSplit[0];
            B = verticalSplit[1];
            C = verticalSplit[2];
            D = verticalSplit[3];
            return addState(moveToCenterOfRectangle(A, B, C, D));
        }
        return addState(badHintSubroutine(hint));
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

    private Movement badHintSubroutine(HalfPlaneHint hint) {
        //return moveToCenterOfRectangle(A, B, C, D); //testing

        Point direction = GEOMETRY_FACTORY.createPoint(twoStepsOrthogonal(hint, centerOfRectangle(A, B, C, D)));
        Movement move = new Movement();
        move.addWayPoint(direction);
        lastHintWasBad = true;
        lastBadHint = hint;
        return move;
    }

    private Coordinate twoStepsOrthogonal(HalfPlaneHint hint, Point P) {
        return twoStepsOrthogonal(hint, P.getCoordinate());
    }

    private Coordinate twoStepsOrthogonal(HalfPlaneHint hint, Coordinate cur_pos) {
        Vector2D hintVector = new Vector2D(hint.getLowerHintPoint().getCoordinate(),
                hint.getUpperHintPoint().getCoordinate());

        hintVector = hintVector.divide(hintVector.length() / 2);

        switch (hint.getDirection()) {
            case up:
                return new Coordinate(cur_pos.getX(), cur_pos.getY() + 2);
            case down:
                return new Coordinate(cur_pos.getX(), cur_pos.getY() - 2);
            case left:
                hintVector = hintVector.rotateByQuarterCircle(1);
            case right:
                hintVector = hintVector.rotateByQuarterCircle(3);
        }
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

    private Movement moveToCenterOfRectangle(Point P1, Point P2, Point P3, Point P4) {
        Movement ret = new Movement();
        ret.addWayPoint(centerOfRectangle(P1, P2, P3, P4));
        return ret;
    }

    private Movement incrementPhase() {
        phase++;
        Point oldA = A;
        Point oldB = B;
        Point oldC = C;
        Point oldD = D;
        setRectToPhase();
        Movement move = rectangleScan(oldA, oldB, oldC, oldD, new Movement());
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
     * Since in the paper the variable names A, B, C and D are used for the by phi transformed points of the current
     * rectangle
     * At, Bt, Ct and Dt in this implementation equate to A, B, C and D in the paper, since the
     * not by phi transformed variables of the current rectangle R are also stored with A, B, C and D.
     * The t signals the transformed state of this variables.
     * hintT equates to the hint (L1', x1')
     *
     * @param curHint
     * @return
     */
    private Movement lastHintBadSubroutine(HalfPlaneHint curHint) {
        Coordinate[] rect = new Coordinate[]{A.getCoordinate(), B.getCoordinate(), C.getCoordinate(), D.getCoordinate()};
        int basicTrans = getBasicTransformation(rect, lastBadHint); // basic transformation
        Coordinate[] transformedRect = phiRectangle(basicTrans, rect);
        Coordinate At = transformedRect[0];
        Coordinate Bt = transformedRect[1];
        Coordinate Ct = transformedRect[2];
        Coordinate Dt = transformedRect[3];
        HalfPlaneHint hintT = phiHint(basicTrans, rect, lastBadHint);

        Coordinate p = centerOfRectangle(transformedRect);
        Coordinate p_apos = twoStepsOrthogonal(hintT, p);

        LineSegment ABt = new LineSegment(At, Bt);
        LineSegment ADt = new LineSegment(At, Dt);
        LineSegment BCt = new LineSegment(Bt, Ct);
        LineSegment CDt = new LineSegment(Ct, Dt);
        LineSegment L1_apos = new LineSegment(hintT.getAnglePointLeft().getCoordinate(),
                hintT.getAnglePointRight().getCoordinate());
        LineSegment L1_doubleApos = new LineSegment(hintT.getAnglePointLeft().getX() + p_apos.getX(),
                hintT.getAnglePointLeft().getY() + p_apos.getY(),
                hintT.getAnglePointRight().getX() + p_apos.getX(),
                hintT.getAnglePointRight().getY() + p_apos.getY());

        Coordinate a = lineWayIntersection(L1_apos, ADt);
        Coordinate d = lineWayIntersection(L1_apos, BCt);
        Coordinate e = new Coordinate(Dt.getX(), d.getY());
        Coordinate d_apos = null;
        if (d != null)
            d_apos = twoStepsOrthogonal(lastBadHint, d);

        Coordinate f = lineWayIntersection(L1_doubleApos, ABt);
        Coordinate j = lineWayIntersection(L1_doubleApos, BCt);

        Coordinate j_apos = new Coordinate(Dt.getX(), j.getY());
        Coordinate t = new Coordinate(f.getX(), D.getY());

        Coordinate m = new Coordinate(At.getX(), p.getY());
        Coordinate m_apos = new Coordinate(At.getX(), p_apos.getY());
        Coordinate k = new Coordinate(Bt.getX(), p.getY());
        Coordinate k_apos = new Coordinate(Bt.getX(), p_apos.getY());

        Coordinate g = new Coordinate(p.getX(), At.getY());
        Coordinate g_apos = new Coordinate(p_apos.getX(), At.getY());
        Coordinate h = new Coordinate(p.getX(), Dt.getY());
        Coordinate h_apos = new Coordinate(p_apos.getX(), Dt.getY());

        Coordinate s, s_apos;
        double p_to_p_apos_x = p_apos.getX() - p.getX(); // the x coordinate of the vector from p to p_apos
        double p_to_p_apos_y = p_apos.getY() - p.getY(); // the y coordinate of the vector from p to p_apos

        LineSegment A_s_apos = new LineSegment(At.getX(), At.getY(),
                At.getX() + p_to_p_apos_x, At.getY() + p_to_p_apos_y);
        // the line from A to s gets constructed by using the line from p to p' (p_apos)
        s = new Coordinate(L1_apos.lineIntersection(A_s_apos));
        s_apos = new Coordinate(L1_doubleApos.lineIntersection(A_s_apos));

        HalfPlaneHint curHintT = phiHint(basicTrans, rect, curHint);
        HalfPlaneHint.Direction x2_apos = curHintT.getDirection();
        LineSegment L2_apos = new LineSegment(curHintT.getAnglePointLeft().getCoordinate(),
                curHintT.getAnglePointRight().getCoordinate());

        // here begins line 24 of the ReduceRectangle routine from the paper:
        Movement move = new Movement();
        Coordinate[] newRectangle = null;

        LineSegment pp_apos = new LineSegment(p, p_apos);
        if (x2_apos == right &&
                normalizePositive(L2_apos.angle()) <= normalizePositive(L1_doubleApos.angle()) &&
                normalizePositive(L2_apos.angle()) > normalizePositive(pp_apos.angle())) {
            newRectangle = arrangeRectangle(phiOtherRectangleInverse(basicTrans, rect,
                    new Coordinate[]{f, Bt, Ct, t}));
        }

        LineSegment m_apos_k_apos = new LineSegment(m_apos, k_apos);
        if (x2_apos == right &&
                normalizePositive(L2_apos.angle()) <= normalizePositive(pp_apos.angle()) &&
                normalizePositive(L2_apos.angle()) > normalizePositive(m_apos_k_apos.angle())) {
            move = rectangleScanPhiReverse(basicTrans, rect, m_apos, k_apos, k, m, move);
            newRectangle = arrangeRectangle(phiOtherRectangleInverse(basicTrans, rect,
                    new Coordinate[]{g, Bt, Ct, h}));
        }
        if ((x2_apos == left || x2_apos == down) &&
                normalizePositive(L2_apos.angle()) <= normalizePositive(m_apos_k_apos.angle()) &&
                normalizePositive(L2_apos.angle()) > normalizePositive(L1_doubleApos.angle())) {

            // rectangleScan(phi_reverse(k, (s, s', d', d))
            move = rectangleScanPhiReverse(basicTrans, rect, s, s_apos, d_apos, d, move);
            // rectangleScan(phi_reverse(k, (m', k', k, m))
            move = rectangleScanPhiReverse(basicTrans, rect, m_apos, k_apos, k, m, move);
            // newRectangle := pkCh
            newRectangle = arrangeRectangle(phiOtherRectangleInverse(basicTrans, rect,
                    new Coordinate[]{p, k, Ct, h}));
        }
        LineSegment h_apos_g_apos = new LineSegment(h_apos, g_apos);
        if (x2_apos == left &&
                normalizePositive(L2_apos.angle()) <= normalizePositive(L1_doubleApos.angle()) &&
                normalizePositive(L2_apos.angle()) > normalizePositive(h_apos_g_apos.angle())) {

            // rectangleScan(phi_reverse(k, (s, s', d', d))
            move = rectangleScanPhiReverse(basicTrans, rect, s, s_apos, d_apos, d, move);
            // rectangleScan(phi_reverse(k, (g, g', h', h))
            // newRectangle := Agpm
            newRectangle = arrangeRectangle(phiOtherRectangleInverse(basicTrans, rect,
                    new Coordinate[]{At, g, p, m}));
        }

        LineSegment p_apos_k = new LineSegment(p_apos, k);

        if ((x2_apos == left &&
                normalizePositive(L2_apos.angle()) <= normalizePositive(h_apos_g_apos.angle()) &&
                normalizePositive(L2_apos.angle()) > normalizePositive(pp_apos.angle())) ||
                (x2_apos == left &&
                        normalizePositive(L2_apos.angle()) <= normalizePositive(pp_apos.angle()) &&
                        normalizePositive(L2_apos.angle()) > normalizePositive(m_apos_k_apos.angle())) ||
                ((x2_apos == up || x2_apos == right) &&
                        normalizePositive(L2_apos.angle()) <= normalizePositive(m_apos_k_apos.angle()) &&
                        normalizePositive(L2_apos.angle()) > normalizePositive(p_apos_k.angle())
                )
        ) {
            // rectangleScan(phireverse(k, (g, g', h', h))
            move = rectangleScanPhiReverse(basicTrans, rect, g, g_apos, h_apos, h, move);
            // newRectangle := ABkm
            newRectangle = arrangeRectangle(phiOtherRectangleInverse(basicTrans, rect,
                    new Coordinate[]{At, Bt, k, m}));
        }
        if (x2_apos == right &&
                normalizePositive(L2_apos.angle()) <= normalizePositive(p_apos_k.angle()) &&
                normalizePositive(L2_apos.angle()) > normalizePositive(L1_doubleApos.angle())) {
            // newRectangle := ABjj'
            newRectangle = arrangeRectangle(phiOtherRectangleInverse(basicTrans, rect,
                    new Coordinate[]{At, Bt, j, j_apos}));
        }

        A = GEOMETRY_FACTORY.createPoint(newRectangle[0]);
        B = GEOMETRY_FACTORY.createPoint(newRectangle[1]);
        C = GEOMETRY_FACTORY.createPoint(newRectangle[2]);
        D = GEOMETRY_FACTORY.createPoint(newRectangle[3]);
        lastHintWasBad = false;
        return move;
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
        Point newAPLeft = (Point) reflection.transform(hint.getAnglePointRight());
        Point newAPRight = (Point) reflection.transform(hint.getAnglePointLeft());
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
        AffineTransformation rot_i = AffineTransformation.rotationInstance(Math.PI * i / 2, r.getX(), r.getY());
        Coordinate ret = new Coordinate();
        return rot_i.transform(P, ret);
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

        AffineTransformation rotHalfPi = AffineTransformation.rotationInstance(Math.PI / 2, r.getX(), r.getY());
        if (i == 0 || i == 2) {
            return rect;
        }
        if (i == 1 || i == 3) {
            //rotate rectangle by pi/2
            Coordinate[] transformed = new Coordinate[4];
            rotHalfPi.transform(rect[1], transformed[0]);
            rotHalfPi.transform(rect[2], transformed[1]);
            rotHalfPi.transform(rect[3], transformed[2]);
            rotHalfPi.transform(rect[0], transformed[3]);
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
        Coordinate r = centerOfRectangle(rect);
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
                GEOMETRY_FACTORY.createPoint(sigmaPoint(i % 4, r, hint.getAnglePointLeft().getCoordinate())),
                GEOMETRY_FACTORY.createPoint(sigmaPoint(i % 4, r, hint.getAnglePointRight().getCoordinate()))
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
        return sigmaPointReverse(i - 3, r, rhoPoint(rect, P));
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
     * Inverts the by rect defined phi , but calculates and returns the inverse points of the points in toTransform.
     *
     * @param i           the index of phi
     * @param rect        the rectangle which defines phi (and therefore also the inverse of phi)
     * @param toTransform
     * @return
     */
    private Coordinate[] phiOtherRectangleInverse(int i, Coordinate[] rect, Coordinate[] toTransform) {
        assertRectangle(rect);
        assertRectangle(toTransform);
        return new Coordinate[]{
                phiPointInverse(i, rect, toTransform[0]),
                phiPointInverse(i, rect, toTransform[1])
        };
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
            HalfPlaneHint testHint = phiHint(i, rect, hint);
            if (testHint.getDirection() == up)
                return i;
            if (testHint.getDirection() == right &&
                    testHint.getUpperHintPoint().getX() < testHint.getLowerHintPoint().getX())
                return i;
        }
        throw new IllegalArgumentException("Somehow there was no basic transformation to be found for this " +
                "rectangle and hint. That is impossible.");
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
        GEOMETRY_FACTORY.getPrecisionModel().makePrecise(rect[0]);
        GEOMETRY_FACTORY.getPrecisionModel().makePrecise(rect[1]);
        GEOMETRY_FACTORY.getPrecisionModel().makePrecise(rect[2]);
        GEOMETRY_FACTORY.getPrecisionModel().makePrecise(rect[3]);

        Coordinate A = null, B = null, C = null, D = null;
        double max_x = Double.MIN_VALUE;
        double max_y = Double.MIN_VALUE;
        double min_x = Double.MAX_VALUE;
        double min_y = Double.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            double x = rect[i].getX();
            double y = rect[i].getY();

            max_x = Math.max(max_x, x);
            max_y = Math.max(max_y, y);
            min_x = Math.min(min_x, x);
            min_y = Math.min(min_y, y);

            if (min_x == x && max_y == y)
                A = rect[i];
            if (max_x == x && max_y == y)
                B = rect[i];
            if (max_x == x && min_y == y)
                C = rect[i];
            if (min_x == x && min_y == y)
                D = rect[i];
        }
        if (A.equals2D(B) || A.equals2D(C) || A.equals2D(D) || B.equals2D(C) || B.equals2D(D) || C.equals2D(D)) {
            throw new IllegalArgumentException("rect is malformed");
        }
        if (A.x != C.x || A.y != B.y || B.x != D.x || C.y != D.y) {
            throw new IllegalArgumentException("rect is not parallel to x an y axis");
        }
        rect[0] = A;
        rect[1] = B;
        rect[2] = C;
        rect[3] = D;

        return rect;
    }
}

