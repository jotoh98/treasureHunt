package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

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
     * Apos is used in such cases (apos for apostrophe), e.g. pApos in this implementation equates to p'
     * in the paper and pDoubleApos euqates to p''.
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
            Coordinate pApos = twoStepsOrthogonal(hintT, p);
            double pToPAposX = pApos.getX() - p.getX(); // the x coordinate of the vector from p to pApos
            double pToPAposY = pApos.getY() - p.getY(); // the y coordinate of the vector from p to pApos

            LineSegment ABt = new LineSegment(At, Bt);
            LineSegment ADt = new LineSegment(At, Dt);
            LineSegment BCt = new LineSegment(Bt, Ct);
            LineSegment CDt = new LineSegment(Ct, Dt);
            LineSegment L1Apos = new LineSegment(hintT.getCenter(),
                    hintT.getRight());
            LineSegment L1DoubleApos = new LineSegment(
                    hintT.getCenter().getX() + pToPAposX,
                    hintT.getCenter().getY() + pToPAposY,
                    hintT.getRight().getX() + pToPAposX,
                    hintT.getRight().getY() + pToPAposY
            );

            Coordinate a = lineWayIntersection(L1Apos, ADt);
            Coordinate d = lineWayIntersection(L1Apos, BCt);
            Coordinate e = null;
            if (d != null)
                e = new Coordinate(Dt.getX(), d.getY());
            Coordinate dApos = null;
            if (d != null)
                dApos = twoStepsOrthogonal(lastBadHint, d);

            Coordinate f = lineWayIntersection(L1DoubleApos, ABt);
            Coordinate j = lineWayIntersection(L1DoubleApos, BCt);

            Coordinate jApos = null;
            if (j != null)
                jApos = new Coordinate(Dt.getX(), j.getY());
            Coordinate t = new Coordinate(f.getX(), Dt.getY());

            Coordinate m = new Coordinate(At.getX(), p.getY());
            Coordinate mApos = new Coordinate(At.getX(), pApos.getY());
            Coordinate k = new Coordinate(Bt.getX(), p.getY());
            Coordinate kApos = new Coordinate(Bt.getX(), pApos.getY());

            Coordinate g = new Coordinate(p.getX(), At.getY());
            Coordinate gApos = new Coordinate(pApos.getX(), At.getY());
            Coordinate h = new Coordinate(p.getX(), Dt.getY());
            Coordinate hApos = new Coordinate(pApos.getX(), Dt.getY());

            Coordinate s, sApos;

            LineSegment AsApos = new LineSegment(At.getX(), At.getY(),
                    At.getX() + pToPAposX, At.getY() + pToPAposY);
            // the line from A to s gets constructed by using the line from p to p' (pApos)
            s = new Coordinate(L1Apos.lineIntersection(AsApos));
            sApos = new Coordinate(L1DoubleApos.lineIntersection(AsApos));

            HalfPlaneHint curHintT = phiHint(basicTrans, rect, curHint);

            HalfPlaneHint.Direction x2Apos = curHintT.getDirection();
            LineSegment L2Apos = new LineSegment(curHintT.getCenter(),
                    curHintT.getRight());

            // here begins line 24 of the ReduceRectangle routine from the paper:
            Coordinate[] newRectangle = null;

            LineSegment ppApos = new LineSegment(p, pApos);
            LineSegment mAposKApos = new LineSegment(mApos, kApos);
            LineSegment hAposGApos = new LineSegment(hApos, gApos);
            LineSegment pAposK = new LineSegment(pApos, k);


            if (x2Apos == right &&
                    lineBetweenClockwise(L2Apos, L1DoubleApos, ppApos)
            ) {
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{f, Bt, Ct, t});
            }
            if (x2Apos == right &&
                    lineBetweenClockwise(L2Apos, ppApos, mAposKApos)
            ) {
                move = rectangleScanPhiReverse(basicTrans, rect, mApos, kApos, k, m, move);
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{g, Bt, Ct, h});
            }
            if ((x2Apos == left || x2Apos == down) &&
                    lineBetweenClockwise(L2Apos, mAposKApos, L1DoubleApos)
            ) {
                // rectangleScan(phi_reverse(k, (s, s', d', d))
                move = rectangleScanPhiReverse(basicTrans, rect, s, sApos, dApos, d, move);
                // rectangleScan(phi_reverse(k, (m', k', k, m))
                move = rectangleScanPhiReverse(basicTrans, rect, mApos, kApos, k, m, move);
                // newRectangle := pkCh
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{p, k, Ct, h});
            }
            if (x2Apos == left &&
                    lineBetweenClockwise(L2Apos, L1DoubleApos, hAposGApos)
            ) {
                // rectangleScan(phi_reverse(k, (s, s', d', d))
                move = rectangleScanPhiReverse(basicTrans, rect, s, sApos, dApos, d, move);
                // rectangleScan(phi_reverse(k, (g, g', h', h))
                // newRectangle := Agpm
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{At, g, p, m});
            }
            if ((x2Apos == left &&
                    lineBetweenClockwise(L2Apos, hAposGApos, ppApos)) ||
                    (x2Apos == left &&
                            lineBetweenClockwise(L2Apos, ppApos, mAposKApos)) ||
                    ((x2Apos == up || x2Apos == right) &&
                            lineBetweenClockwise(L2Apos, mAposKApos, pAposK)
                    )
            ) {
                // rectangleScan(phireverse(k, (g, g', h', h))
                move = rectangleScanPhiReverse(basicTrans, rect, g, gApos, hApos, h, move);
                // newRectangle := ABkm
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{At, Bt, k, m});
            }
            if (x2Apos == right &&
                    lineBetweenClockwise(L2Apos, pAposK, L1DoubleApos)
            ) {
                // newRectangle := ABjj'
                newRectangle = phiOtherRectangleInverse(basicTrans, rect,
                        new Coordinate[]{At, Bt, j, jApos});
            }

            strategy.A = GEOMETRY_FACTORY.createPoint(newRectangle[0]);
            strategy.B = GEOMETRY_FACTORY.createPoint(newRectangle[1]);
            strategy.C = GEOMETRY_FACTORY.createPoint(newRectangle[2]);
            strategy.D = GEOMETRY_FACTORY.createPoint(newRectangle[3]);
            strategy.lastHintWasBad = false;
            return moveToCenterOfRectangle(strategy.A, strategy.B, strategy.C, strategy.D, move);
        } catch (Exception ee) {
            throw processError(ee, strategy, rect, lastBadHint, curHint);
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

    static private RuntimeException processError(Exception e, StrategyFromPaper s, Coordinate[] rect, HalfPlaneHint lastBadHint, HalfPlaneHint curHint) {
        Point A = s.A;
        Point B = s.B;
        Point C = s.C;
        Point D = s.D;
        String message = "A= (" + A.getX() + ", " + A.getY() + ")\n"
                + " B= (" + B.getX() + ", " + B.getY() + ")\n"
                + " C= (" + C.getX() + ", " + C.getY() + ")\n"
                + " D= (" + D.getX() + ", " + D.getY() + ")";
        for (int i = 0; i < rect.length; i++)
            message.concat("rect[" + i + "]= " + rect[i] + "\n");
        message.concat("lastBadHint p1= " + lastBadHint.getCenter() + "lastHint p2= " +
                lastBadHint.getRight() + "\n");
        message.concat("curHint p1= " + curHint.getCenter() + "curHint p2= " +
                curHint.getRight());
        return new RuntimeException(message, e);
    }
}
