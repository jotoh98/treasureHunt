package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils.centerOfRectangle;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils.twoStepsOrthogonal;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.StatusMessages.addCaseDescriptionToStatus;
import static com.treasure.hunt.utils.JTSUtils.GEOMETRY_FACTORY;
import static com.treasure.hunt.utils.JTSUtils.lineWayIntersection;

/**
 * @author Rank
 */
@Slf4j  //todo rm
public class LastHintBadSubroutine {
    /**
     * Variable names are equivalent to the paper, but since a', d', etc. is not a valid variable name in Java,
     * Apos gets added to the variable name in such cases (apos for apostrophe).
     * E.g. pApos in this implementation equates to p' in the paper and pDoubleApos equates to p''.
     */
    private HalfPlaneHint.Direction x2Apos;
    @Getter
    private int basicTransformation;
    @Getter
    private Coordinate[] currentRectangle;
    @Getter
    private LineSegment L1DoubleApos;
    @Getter
    private StrategyFromPaper strategy;
    private LineSegment AB, AD, BC, CD, L1Apos, L2Apos, ppApos, mAposKApos, hAposGApos, pAposK;
    private Coordinate p, pApos, a, d, e, dApos, f, j, jApos, t, m, mApos, k, kApos, g, gApos, h, hApos, s, sApos;
    private Coordinate A, B, C, D;

    /**
     * The result of applying phi, defined by basicTransformation on the hint received before the current hint
     * (which is the bad hint)
     */
    private HalfPlaneHint lastHintT;

    public LastHintBadSubroutine(StrategyFromPaper strategy) {
        this.strategy = strategy;
    }

    /**
     * Initializes the various variables.
     * T is added to variable-names where the variables got transformed to match a basicTransformation
     * (e.g. the current hint in its transformed state is called curHintT)
     */
    private void initializeVariables(HalfPlaneHint currentHint, HalfPlaneHint lastBadHint) {
        currentRectangle = new Coordinate[]{strategy.searchAreaCornerA.getCoordinate(), strategy.searchAreaCornerB.getCoordinate(),
                strategy.searchAreaCornerC.getCoordinate(), strategy.searchAreaCornerD.getCoordinate()};

        basicTransformation = RoutinesFromPaper.getBasicTransformation(currentRectangle, lastBadHint);

        Coordinate[] transformedRect = phiRectangle(basicTransformation, currentRectangle);
        A = transformedRect[0];
        B = transformedRect[1];
        C = transformedRect[2];
        D = transformedRect[3];
        lastHintT = phiHint(basicTransformation, currentRectangle, lastBadHint);

        p = centerOfRectangle(transformedRect);
        pApos = twoStepsOrthogonal(lastHintT, p);
        double pToPAposX = pApos.getX() - p.getX(); // the x coordinate of the vector from p to pApos
        double pToPAposY = pApos.getY() - p.getY(); // the y coordinate of the vector from p to pApos

        AB = new LineSegment(A, B);
        AD = new LineSegment(A, D);
        BC = new LineSegment(B, C);
        CD = new LineSegment(C, D);
        L1Apos = lastHintT.getHalfPlaneLine();
        L1DoubleApos = new LineSegment(
                lastHintT.getCenter().getX() + pToPAposX,
                lastHintT.getCenter().getY() + pToPAposY,
                lastHintT.getRight().getX() + pToPAposX,
                lastHintT.getRight().getY() + pToPAposY
        );

        a = lineWayIntersection(L1Apos, AD);
        d = lineWayIntersection(L1Apos, BC);
        e = null;
        if (d != null) {
            e = new Coordinate(D.getX(), d.getY());
        }
        dApos = null;
        if (d != null) {
            dApos = twoStepsOrthogonal(lastHintT, d);
        }

        f = lineWayIntersection(L1DoubleApos, AB);
        j = lineWayIntersection(L1DoubleApos, BC);

        jApos = null;
        if (j != null) {
            jApos = new Coordinate(D.getX(), j.getY());
        }
        t = new Coordinate(f.getX(), D.getY());

        m = new Coordinate(A.getX(), p.getY());
        mApos = new Coordinate(A.getX(), pApos.getY());
        k = new Coordinate(B.getX(), p.getY());
        kApos = new Coordinate(B.getX(), pApos.getY());

        g = new Coordinate(p.getX(), A.getY());
        gApos = new Coordinate(pApos.getX(), A.getY());
        h = new Coordinate(p.getX(), D.getY());
        hApos = new Coordinate(pApos.getX(), D.getY());

        LineSegment AsApos = new LineSegment(A.getX(), A.getY(),
                A.getX() + pToPAposX, A.getY() + pToPAposY);
        // the line from A to s gets constructed by using the line from p to p' (pApos)
        s = new Coordinate(L1Apos.lineIntersection(AsApos));
        sApos = new Coordinate(L1DoubleApos.lineIntersection(AsApos));

        HalfPlaneHint curHintT = phiHint(basicTransformation, currentRectangle, currentHint);

        x2Apos = curHintT.getDirection();
        L2Apos = new LineSegment(curHintT.getCenter(),
                curHintT.getRight());

        ppApos = new LineSegment(p, pApos);
        mAposKApos = new LineSegment(mApos, kApos);
        hAposGApos = new LineSegment(hApos, gApos);
        pAposK = new LineSegment(pApos, k);
    }

    /**
     * If the last hint was bad, this function can be called and lastBadHint has to be set accordingly.
     * The function equals the "else"-part of the first if-condition in Algorithm 3 (Function ReduceRectangle(R))
     * in the paper.
     *
     * @return The move to scan various areas so that A,B,C and D can be updated to a smaller rectangle (or the treasure
     * is found)
     */
    public SearchPath lastHintBadSubroutine(HalfPlaneHint currentHint,
                                            HalfPlaneHint lastBadHint, SearchPath move, boolean changeABCD) {

        initializeVariables(currentHint, lastBadHint);
        int caseIndex = -1;

        // here begins line 24 of the ReduceRectangle routine from the paper:
        Coordinate[] newRectangle = null;

        if (x2Apos == right &&
                lineBetweenClockwise(L2Apos, L1DoubleApos, ppApos)
        ) {
            caseIndex = 1;
            newRectangle = phiOtherRectangleInverse(basicTransformation, currentRectangle,
                    new Coordinate[]{f, B, C, t});
        } else if (x2Apos == right &&
                lineBetweenClockwise(L2Apos, ppApos, mAposKApos)
        ) {
            caseIndex = 2;
            move = rectangleScanPhiReverse(basicTransformation, currentRectangle, mApos, kApos, k, m, move, strategy);
            newRectangle = phiOtherRectangleInverse(basicTransformation, currentRectangle,
                    new Coordinate[]{g, B, C, h});
        } else if (x2Apos == down || (x2Apos == left &&
                lineBetweenClockwise(L2Apos, mAposKApos, L1DoubleApos))
        ) {
            caseIndex = 3;
            // rectangleScan(phi_reverse(k, (s, s', d', d))
            move = rectangleScanPhiReverse(basicTransformation, currentRectangle, s, sApos, dApos, d, move, strategy);
            // rectangleScan(phi_reverse(k, (m', k', k, m))
            move = rectangleScanPhiReverse(basicTransformation, currentRectangle, mApos, kApos, k, m, move, strategy);
            // newRectangle := pkCh
            newRectangle = phiOtherRectangleInverse(basicTransformation, currentRectangle,
                    new Coordinate[]{p, k, C, h});
        } else if (x2Apos == left &&
                lineBetweenClockwise(L2Apos, L1DoubleApos, hAposGApos)
        ) {
            caseIndex = 4;
            // rectangleScan(phi_reverse(k, (s, s', d', d))
            move = rectangleScanPhiReverse(basicTransformation, currentRectangle, s, sApos, dApos, d, move, strategy);
            // rectangleScan(phi_reverse(k, (g, g', h', h))
            move = rectangleScanPhiReverse(basicTransformation, currentRectangle, g, gApos, hApos, h, move, strategy);
            // newRectangle := Agpm
            newRectangle = phiOtherRectangleInverse(basicTransformation, currentRectangle,
                    new Coordinate[]{A, g, p, m});
        } else if (x2Apos == up || (x2Apos == left &&
                lineBetweenClockwise(L2Apos, hAposGApos, ppApos)) ||
                (x2Apos == left &&
                        lineBetweenClockwise(L2Apos, ppApos, mAposKApos)) ||
                ((x2Apos == right) &&
                        lineBetweenClockwise(L2Apos, mAposKApos, pAposK)
                )
        ) {
            caseIndex = 5;
            // rectangleScan(phireverse(k, (g, g', h', h))
            move = rectangleScanPhiReverse(basicTransformation, currentRectangle, g, gApos, hApos, h, move, strategy);
            // newRectangle := ABkm
            newRectangle = phiOtherRectangleInverse(basicTransformation, currentRectangle,
                    new Coordinate[]{A, B, k, m});
        } else if (x2Apos == right &&
                lineBetweenClockwise(L2Apos, pAposK, L1DoubleApos)
        ) {
            caseIndex = 6;
            // newRectangle := ABjj'
            newRectangle = phiOtherRectangleInverse(basicTransformation, currentRectangle,
                    new Coordinate[]{A, B, j, jApos});
        }

        log.debug("caseIndex = " + caseIndex + " basicTransformation= " + basicTransformation);//todo rm

        if (caseIndex == -1) {
            throw new AssertionError("No case got used.\n basicTransformation = " + basicTransformation +
                    "\n current rectangle " + strategy.searchAreaCornerA + "\n " +
                    strategy.searchAreaCornerB + "\n " +
                    strategy.searchAreaCornerC + "\n " +
                    strategy.searchAreaCornerD + "\n current hint =\n " +
                    currentHint.getCenter() + ", " + currentHint.getRight() + "\n last hint =\n " +
                    lastBadHint.getCenter() + ", " + lastBadHint.getRight()
            );
        }
        addCaseDescriptionToStatus(move, basicTransformation, caseIndex, this);

        if(changeABCD) {
            strategy.searchAreaCornerA = GEOMETRY_FACTORY.createPoint(newRectangle[0]);
            strategy.searchAreaCornerB = GEOMETRY_FACTORY.createPoint(newRectangle[1]);
            strategy.searchAreaCornerC = GEOMETRY_FACTORY.createPoint(newRectangle[2]);
            strategy.searchAreaCornerD = GEOMETRY_FACTORY.createPoint(newRectangle[3]);
        }
        return move;
    }

    /**
     * Returns true if line is clockwise between between1 (included) and between2 (excluded).
     * Its taken for granted that line between1 and between2 meet in one Point.
     *
     * @return if line is clockwise between between1 (included) and between2 (excluded)
     */
    private boolean lineBetweenClockwise(LineSegment line, LineSegment between1, LineSegment between2) {
        GEOMETRY_FACTORY.getPrecisionModel().makePrecise(line.p0);
        GEOMETRY_FACTORY.getPrecisionModel().makePrecise(line.p1);
        GEOMETRY_FACTORY.getPrecisionModel().makePrecise(between1.p0);
        GEOMETRY_FACTORY.getPrecisionModel().makePrecise(between1.p1);
        GEOMETRY_FACTORY.getPrecisionModel().makePrecise(between2.p0);
        GEOMETRY_FACTORY.getPrecisionModel().makePrecise(between2.p1);

        double minLineAngle = GeometricUtils.minimumAngleToXAxis(line);
        double minBetween1Angle = GeometricUtils.minimumAngleToXAxis(between1);
        double minBetween2Angle = GeometricUtils.minimumAngleToXAxis(between2);
        if (minBetween1Angle == 0) {
            minBetween1Angle = Math.PI;
        }

        return minLineAngle <= minBetween1Angle && minLineAngle > minBetween2Angle;
    }
}
