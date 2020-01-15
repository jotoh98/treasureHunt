package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.util.Arrays;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper.*;
import static com.treasure.hunt.utils.JTSUtils.GEOMETRY_FACTORY;
import static com.treasure.hunt.utils.JTSUtils.lineWayIntersection;
import static org.locationtech.jts.algorithm.Angle.normalizePositive;

/**
 * @author bsen
 */
public class BadHintSubroutine {


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
    static Movement lastHintBadSubroutine(StrategyFromPaper strategy, HalfPlaneHint curHint, HalfPlaneHint lastBadHint,
                                          Movement move) {
        Coordinate[] rect = new Coordinate[]{strategy.A.getCoordinate(), strategy.B.getCoordinate(),
                strategy.C.getCoordinate(), strategy.D.getCoordinate()};
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
            LineSegment L1_apos = new LineSegment(hintT.getLeftPoint(),
                    hintT.getRightPoint());
            LineSegment L1_doubleApos = new LineSegment(
                    hintT.getLeftPoint().getX() + p_to_p_apos_x,
                    hintT.getLeftPoint().getY() + p_to_p_apos_y,
                    hintT.getRightPoint().getX() + p_to_p_apos_x,
                    hintT.getRightPoint().getY() + p_to_p_apos_y
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
            System.out.println("curHintTransformed (" + curHintT.getLeftPoint() + ", " + curHintT.getRightPoint() + ")");
            System.out.println("transformedHint (" + hintT.getLeftPoint() + ", " + hintT.getRightPoint() + ")");
            System.out.println("transformedRect " + Arrays.toString(transformedRect)); // testing

            HalfPlaneHint.Direction x2_apos = curHintT.getDirection();
            LineSegment L2_apos = new LineSegment(curHintT.getLeftPoint(),
                    curHintT.getRightPoint());

            // here begins line 24 of the ReduceRectangle routine from the paper:
            Coordinate[] newRectangle = null;

            LineSegment pp_apos = new LineSegment(p, p_apos);
            LineSegment m_apos_k_apos = new LineSegment(m_apos, k_apos);
            LineSegment h_apos_g_apos = new LineSegment(h_apos, g_apos);
            LineSegment p_apos_k = new LineSegment(p_apos, k);


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
            } //else
            if (x2_apos == right &&
                    lineBetweenClockwise(L2_apos, pp_apos, m_apos_k_apos)
            ) {
                System.out.println("--------------------------------------------------zweiter fall"); //testing
                move = rectangleScanPhiReverse(basicTrans, rect, m_apos, k_apos, k, m, move);
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{g, Bt, Ct, h});
            } //else
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
            } //else
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
            } //else
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
            } //else
            if (x2_apos == right &&
                    lineBetweenClockwise(L2_apos, p_apos_k, L1_doubleApos)
            ) {
                System.out.println("--------------------------------------------------sechster fall"); //testing
                // newRectangle := ABjj'
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{At, Bt, j, j_apos});
            }

            strategy.A = GEOMETRY_FACTORY.createPoint(newRectangle[0]);
            strategy.B = GEOMETRY_FACTORY.createPoint(newRectangle[1]);
            strategy.C = GEOMETRY_FACTORY.createPoint(newRectangle[2]);
            strategy.D = GEOMETRY_FACTORY.createPoint(newRectangle[3]);
            strategy.lastHintWasBad = false;
            return moveToCenterOfRectangle(strategy.A, strategy.B, strategy.C, strategy.D, move);
        } catch (Exception ee) {
            printRect(strategy, rect, lastBadHint, curHint);
            throw ee;
        }

    }
    //test end

    /**
     * Returns true if line is clockwise between between1 (included) and between2 (excluded).
     * Its taken for granted that line between1 and between2 meet in one Point.
     *
     * @param line
     * @param between1
     * @param between2
     * @return if line is clockwise between between1 (included) and between2 (excluded)
     */
    static boolean lineBetweenClockwise(LineSegment line, LineSegment between1, LineSegment between2) {
        LineSegment lineReverse = new LineSegment(line.p1, line.p0);
        LineSegment between2reverse = new LineSegment(between2.p1, between2.p0);
        double angleBetween1 = between1.angle();
        double maxAngleLineBetween1 = Math.max(normalizePositive(line.angle() - angleBetween1), normalizePositive(lineReverse.angle() - angleBetween1));
        double maxAngleBetween2and1 = Math.max(normalizePositive(between2.angle() - angleBetween1), normalizePositive(between2reverse.angle() - angleBetween1));
        if (maxAngleLineBetween1 == 0)
            return true;
        return maxAngleBetween2and1 < maxAngleLineBetween1;
    }

    //just for testing
    static private void printRect(StrategyFromPaper s, Coordinate[] rect, HalfPlaneHint lastBadHint, HalfPlaneHint curHint) {
        Point A = s.A;
        Point B = s.B;
        Point C = s.C;
        Point D = s.D;
        System.out.println("A= (" + A.getX() + ", " + A.getY() + ")");
        System.out.println(" B= (" + B.getX() + ", " + B.getY() + ")");
        System.out.println(" C= (" + C.getX() + ", " + C.getY() + ")");
        System.out.println(" D= (" + D.getX() + ", " + D.getY() + ")");
        for (int i = 0; i < rect.length; i++)
            System.out.println("rect[" + i + "]= " + rect[i]);
        System.out.println("lastBadHint p1= " + lastBadHint.getLeftPoint() + "lastHint p2= " +
                lastBadHint.getRightPoint());
        System.out.println("curHint p1= " + curHint.getLeftPoint() + "curHint p2= " +
                curHint.getRightPoint());
    }


}
