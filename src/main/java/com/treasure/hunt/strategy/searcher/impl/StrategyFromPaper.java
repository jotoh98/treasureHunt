package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Value;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.math.Vector2D;

import static com.treasure.hunt.strategy.geom.GeometryType.CURRENT_PHASE;
import static com.treasure.hunt.strategy.geom.GeometryType.CURRENT_RECTANGLE;
import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.utils.JTSUtils.GEOMETRY_FACTORY;
import static com.treasure.hunt.utils.JTSUtils.lineWayIntersection;

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
            return twoHintsSubroutine(hint);

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

    private Coordinate twoStepsOrthogonal(HalfPlaneHint hint, Point cur_pos) {
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
        Movement move = rectangleScan(oldA, oldB, oldC, oldD);
        move.addWayPoint(centerOfRectangle(A, B, C, D));
        return move;
    }

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

    private void setRectToPhase() {
        Coordinate[] rect = phaseRectangle();
        A = GEOMETRY_FACTORY.createPoint(rect[0]);
        B = GEOMETRY_FACTORY.createPoint(rect[1]);
        C = GEOMETRY_FACTORY.createPoint(rect[2]);
        D = GEOMETRY_FACTORY.createPoint(rect[3]);
    }

    private Movement rectangleScan(Point A, Point B, Point C, Point D) {
        Movement move = new Movement();

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

    private Movement twoHintsSubroutine(HalfPlaneHint curHint) {
        // TODO
        // dann die Fälle des Papers durchgehen und dementsprechend returnen

        // variable names are equivalent to the ones used in the ReduceRectangle routine in the paper
        // (apos for apostrophe)
        // since A, B, C, D and the hints already exist in normal representation, the transformed ones have a t in the
        // variable name
        RectangleHintPair rp = new RectangleHintPair(A, B, C, D, lastBadHint);
        int phi_k = getBasicTransformation(rp);
        RectangleHintPair transformed_rp = phi(phi_k, rp);
        Point At = transformed_rp.getA();
        Point Bt = transformed_rp.getB();
        Point Ct = transformed_rp.getC();
        Point Dt = transformed_rp.getD();
        HalfPlaneHint hintT = transformed_rp.getHint();

        Point p = centerOfRectangle(At, Bt, Ct, Dt);
        Coordinate p_apos = twoStepsOrthogonal(hintT, p);
        // L_1_apos_dash = (p, p_apos)

        LineSegment ABt = new LineSegment(At.getCoordinate(), Bt.getCoordinate());
        LineSegment ADt = new LineSegment(At.getCoordinate(), Dt.getCoordinate());
        LineSegment BCt = new LineSegment(Bt.getCoordinate(), Ct.getCoordinate());
        LineSegment CDt = new LineSegment(Ct.getCoordinate(), Dt.getCoordinate());
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
            d_apos = twoStepsOrthogonal(lastBadHint, GEOMETRY_FACTORY.createPoint(d));

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
        Coordinate h_dash = new Coordinate(p_apos.getX(), Dt.getY());

        Coordinate s, s_apos;
        double p_to_p_apos_x = p_apos.getX() - p.getX(); // the x coordinate of the vector from p to p_apos
        double p_to_p_apos_y = p_apos.getY() - p.getY(); // the y coordinate of the vector from p to p_apos

        LineSegment A_s_apos = new LineSegment(At.getX(), At.getY(),
                At.getX() + p_to_p_apos_x, At.getY() + p_to_p_apos_y);
        // the line from A to s gets constructed by using the line from p to p' (p_apos)
        s = new Coordinate(L1_apos.lineIntersection(A_s_apos));
        s_apos = new Coordinate(L1_doubleApos.lineIntersection(A_s_apos));

        RectangleHintPair curHintPair = new RectangleHintPair(A, B, C, D, curHint);
        HalfPlaneHint curHintT = phi(phi_k, curHintPair).getHint();
        HalfPlaneHint.Direction x2_apos = curHintT.getDirection();
        LineSegment L2_apos = new LineSegment(curHintT.getAnglePointLeft().getCoordinate(),
                curHintT.getAnglePointRight().getCoordinate());
        // here begins line 24 of the ReduceRectangle routine from the paper:
        // test wether L2_apos is between (p, p_apos) and (m_apos, k_apos)
        //Angle.angleBetweenOriented(curHintT.getAnglePointRight().getCoordinate(), p.getCoordinate(),);

        //if(curHintT.getDirection()==right && )

        return null;
    }

    /**
     * Returnes the rectangle R of rp and the hint of rp which are mirrored in H, a vertical line
     * through the center of R.
     *
     * @param rp
     * @return
     */
    private RectangleHintPair rho(RectangleHintPair rp) {
        Point A = rp.getA();
        Point B = rp.getB();
        Point C = rp.getC();
        Point D = rp.getD();

        LineString AD = JTSUtils.createLineString(A, D);
        LineString BC = JTSUtils.createLineString(B, C);
        Point centerAD = AD.getCentroid();
        Point centerBC = BC.getCentroid();

        AffineTransformation reflection = AffineTransformation.reflectionInstance(centerAD.getX(), centerAD.getY(),
                centerBC.getX(), centerBC.getY());

        Point newAPLeft = (Point) reflection.transform(rp.getHint().getAnglePointRight());
        Point newAPRight = (Point) reflection.transform(rp.getHint().getAnglePointLeft());
        HalfPlaneHint newHint = new HalfPlaneHint(newAPLeft, newAPRight);
        return new RectangleHintPair(rp.getA(), rp.getB(), rp.getC(), rp.getD(), newHint);
    }

    private RectangleHintPair sigma(int i, RectangleHintPair rp) {
        if (i < 0 || i > 3) {
            throw new IllegalArgumentException("sigma was called with i not in [0,3]");
        }

        Point A = rp.getA();
        Point B = rp.getB();
        Point C = rp.getC();
        Point D = rp.getD();

        Point newA = null;
        Point newB = null;
        Point newC = null;
        Point newD = null;

        Point r = centerOfRectangle(A, B, C, D);
        AffineTransformation rotHalfPi = AffineTransformation.rotationInstance(Math.PI / 2, r.getX(), r.getY());
        AffineTransformation rot_i = AffineTransformation.rotationInstance(Math.PI * i / 2, r.getX(), r.getY());

        if (i == 0 || i == 2) {
            newA = A;
            newB = B;
            newC = C;
            newD = D;
        }
        if (i == 1 || i == 3) {
            //rotate rectangle by pi/2
            newA = (Point) rotHalfPi.transform(B);
            newB = (Point) rotHalfPi.transform(C);
            newC = (Point) rotHalfPi.transform(D);
            newD = (Point) rotHalfPi.transform(A);
        }
        Point newAPLeft = (Point) rot_i.transform(rp.getHint().getAnglePointLeft());
        Point newAPRight = (Point) rot_i.transform(rp.getHint().getAnglePointRight());
        HalfPlaneHint hint = new HalfPlaneHint(newAPLeft, newAPRight);
        return new RectangleHintPair(newA, newB, newC, newD, hint);
    }

    private RectangleHintPair sigmaReverse(int i, RectangleHintPair rp) {
        return sigma(3 - i, rp);
    }

    private RectangleHintPair phi(int i, RectangleHintPair rp) {
        if (i < 0 || i > 7)
            throw new IllegalArgumentException("i must be in [0,7]");
        if (i < 4)
            return sigma(i, rp);
        return rho(sigma(i - 4, rp));
    }

    private RectangleHintPair reversePhi(int i, RectangleHintPair rp) {
        if (i < 0 || i > 7)
            throw new IllegalArgumentException("i must be in [0,7]");
        if (i < 4)
            return sigmaReverse(i, rp);
        return sigmaReverse(i - 4, rho(rp));
    }

    private int getBasicTransformation(RectangleHintPair rp) {
        for (int i = 0; i <= 7; i++) {
            HalfPlaneHint testHint = phi(i, rp).getHint();
            HalfPlaneHint.Direction testDir = testHint.getDirection();
            if (testDir == up)
                return i;
            if (testDir == right && testHint.getUpperHintPoint().getX() < testHint.getLowerHintPoint().getX())
                return i;
        }
        throw new IllegalArgumentException("Somehow there was no basic transformation to be found for this " +
                "RectangleHintPair. This is not possible.");
    }

    @Value
    private class RectangleHintPair {
        Point A;
        Point B;
        Point C;
        Point D;
        HalfPlaneHint hint;
    }

}

