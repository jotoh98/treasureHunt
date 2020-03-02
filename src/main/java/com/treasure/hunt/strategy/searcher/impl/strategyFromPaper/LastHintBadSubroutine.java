package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.geom.StatusMessageItem;
import com.treasure.hunt.strategy.geom.StatusMessageType;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils.*;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.HintQuality.none;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper.*;
import static com.treasure.hunt.utils.JTSUtils.GEOMETRY_FACTORY;
import static com.treasure.hunt.utils.JTSUtils.lineWayIntersection;
import static org.locationtech.jts.algorithm.Angle.normalizePositive;

/**
 * @author bsen
 */
class LastHintBadSubroutine {

    @Getter
    static private LastHintBadSubroutine instance = new LastHintBadSubroutine();

    /**
     * Variable names are equivalent to the paper, but since a', d', etc. is not a valid variable name in Java,
     * Apos gets added to the variable name in such cases (apos for apostrophe).
     * E.g. pApos in this implementation equates to p' in the paper and pDoubleApos equates to p''.
     */
    HalfPlaneHint.Direction x2Apos;
    private int basicTransformation;
    private Coordinate[] rect;
    private LineSegment AB, AD, BC, CD, L1Apos, L1DoubleApos, L2Apos, AsApos, ppApos, mAposKApos, hAposGApos, pAposK;
    private Coordinate p, pApos, a, d, e, dApos, f, j, jApos, t, m, mApos, k, kApos, g, gApos, h, hApos, s, sApos;
    private Coordinate A, B, C, D;

    /**
     * The result of applying phi, defined by basicTransformation on the hint received before the current hint
     * (which is the bad hint)
     */
    private HalfPlaneHint lastHintT;

    private LastHintBadSubroutine() {
    }

    /**
     * Initializes the various variables.
     * T is added to variable-names where the variables got transformed to match a basicTransformation
     * (e.g. the current hint in its transformed state is called curHintT)
     *
     * @param strategy
     * @param curHint
     * @param lastBadHint
     */
    private void initializeVariables(StrategyFromPaper strategy, HalfPlaneHint curHint,
                                     HalfPlaneHint lastBadHint) {
        rect = new Coordinate[]{strategy.searchAreaCornerA.getCoordinate(), strategy.searchAreaCornerB.getCoordinate(),
                strategy.searchAreaCornerC.getCoordinate(), strategy.searchAreaCornerD.getCoordinate()};

        basicTransformation = getBasicTransformation(rect, lastBadHint);

        Coordinate[] transformedRect = phiRectangle(basicTransformation, rect);
        A = transformedRect[0];
        B = transformedRect[1];
        C = transformedRect[2];
        D = transformedRect[3];
        lastHintT = phiHint(basicTransformation, rect, lastBadHint);

        p = centerOfRectangle(transformedRect);
        pApos = twoStepsOrthogonal(lastHintT, p);
        double pToPAposX = pApos.getX() - p.getX(); // the x coordinate of the vector from p to pApos
        double pToPAposY = pApos.getY() - p.getY(); // the y coordinate of the vector from p to pApos

        AB = new LineSegment(A, B);
        AD = new LineSegment(A, D);
        BC = new LineSegment(B, C);
        CD = new LineSegment(C, D);
        L1Apos = new LineSegment(lastHintT.getCenter(),
                lastHintT.getRight());
        L1DoubleApos = new LineSegment(
                lastHintT.getCenter().getX() + pToPAposX,
                lastHintT.getCenter().getY() + pToPAposY,
                lastHintT.getRight().getX() + pToPAposX,
                lastHintT.getRight().getY() + pToPAposY
        );

        a = lineWayIntersection(L1Apos, AD);
        d = lineWayIntersection(L1Apos, BC);
        e = null;
        if (d != null)
            e = new Coordinate(D.getX(), d.getY());
        dApos = null;
        if (d != null)
            dApos = twoStepsOrthogonal(lastBadHint, d);

        f = lineWayIntersection(L1DoubleApos, AB);
        j = lineWayIntersection(L1DoubleApos, BC);

        jApos = null;
        if (j != null)
            jApos = new Coordinate(D.getX(), j.getY());
        t = new Coordinate(f.getX(), D.getY());

        m = new Coordinate(A.getX(), p.getY());
        mApos = new Coordinate(A.getX(), pApos.getY());
        k = new Coordinate(B.getX(), p.getY());
        kApos = new Coordinate(B.getX(), pApos.getY());

        g = new Coordinate(p.getX(), A.getY());
        gApos = new Coordinate(pApos.getX(), A.getY());
        h = new Coordinate(p.getX(), D.getY());
        hApos = new Coordinate(pApos.getX(), D.getY());

        AsApos = new LineSegment(A.getX(), A.getY(),
                A.getX() + pToPAposX, A.getY() + pToPAposY);
        // the line from A to s gets constructed by using the line from p to p' (pApos)
        s = new Coordinate(L1Apos.lineIntersection(AsApos));
        sApos = new Coordinate(L1DoubleApos.lineIntersection(AsApos));

        HalfPlaneHint curHintT = phiHint(basicTransformation, rect, curHint);

        x2Apos = curHintT.getDirection();
        L2Apos = new LineSegment(curHintT.getCenter(),
                curHintT.getRight());


        ppApos = new LineSegment(p, pApos);
        mAposKApos = new LineSegment(mApos, kApos);
        hAposGApos = new LineSegment(hApos, gApos);
        pAposK = new LineSegment(pApos, k);
    }

    private String[] getLettersOfTransformedRectangle(int basicTransformation) {
        /**
         * The rectangle got transformed by phi (with basicTransformation as index)
         * The old rectangle gets called ABCD in the following, with A being the corner-point on the top left,
         * B being the point on the top right, etc.
         * In the following transformedRectangle[0] is the top left corner from the transformed rectangle
         * (this could be A,B,C or D dependant on the basicTransformation's value)
         * transformedRectangle[0] is the top left corner,
         * transformedRectangle[1] is the top right corner,
         * transformedRectangle[2] is the bottom right corner and
         * transformedRectangle[3] is the bottom left corner.
         */
        String transformedRectangleString;
        String[] transformedRectangle;
        switch (basicTransformation) {
            case 0:
                transformedRectangleString = "ABCD";
                break;
            case 1:
                transformedRectangleString = "DABC";
                break;
            case 2:
                transformedRectangleString = "CDAB";
                break;
            case 3:
                transformedRectangleString = "BCDA";
                break;
            case 4:
                transformedRectangleString = "BADC";
                break;
            case 5:
                transformedRectangleString = "ADCB";
                break;
            case 6:
                transformedRectangleString = "DCBA";
                break;
            case 7:
                transformedRectangleString = "CBAD";
                break;
            default:
                throw new AssertionError("basicTransformation should be in {0 ,1 ,2 ,3 ,4 ,5 ,6 ,7} " +
                        "but equals " + basicTransformation);
        }
        transformedRectangle = transformedRectangleString.split("");
        return transformedRectangle;
    }

    private void addCaseDescriptionToStatus(SearchPath move, int basicTransformation, int caseIndex,
                                            StrategyFromPaper strategy) {
        String[] transformedRectangle = getLettersOfTransformedRectangle(basicTransformation);
        String statusMessage = "Let ABCD be the previous rectangle's corners, with\n" +// TODO vllt noch schreiben, wie das rectangle gezeichnet wird (Farbe)
                "A being the corner on the top left,\n" +
                "B being the corner on the top right,\n" +
                "C being the corner on the bottom right and\n" +
                "D being the corner on the bottom left.\n" +
                "\n" +
                "Let H1 be the current hint (which was not yet analyzed)\n" +
                "Let H2 be the hint before H1\n" +
                "Let H3 be the hint before H2 (the bad hint due to which the player went 2 Steps orthogonal to the " +
                "hintline)\n\n";

        // the defining Strings for the variables:
        String L1DoubleAposDef = "Let L1'' be the line parallel to the line of H3, and going through the point where " +
                "the player was when he received H2.\n" +
                "(The name is taken out of the paper)\n";

        String pDef = "Let p be the piont where H3 was received.\n";
        String pAposDef = "Let p' be the point where H2 was received.\n";

        String mDef = "Let m be the orthogonal projection of p onto segment " + transformedRectangle[0] +
                transformedRectangle[3] + "\n";
        String kDef = "Let k be the orthogonal projection of p oonto segment " + transformedRectangle[1] +
                transformedRectangle[2] + "\n";
        String mAposDef = "Let m' be the orthogonal projection of p' onto segment " + transformedRectangle[0] +
                transformedRectangle[3] + "\n";
        String kAposDef = "Let m' be the orthogonal projection of p' onto segment " + transformedRectangle[0] +
                transformedRectangle[3] + "\n";

        String gDef = "Let g be the orthogonal projection of p onto segment " + transformedRectangle[0] +
                transformedRectangle[1] + ".\n";
        String hDef = "Let h be the orthogonal projection of p onto segment " + transformedRectangle[3] +
                transformedRectangle[2] + ".\n";
        String gAposDef = "Let g' be the orthogonal projection of p' onto segment " + transformedRectangle[0] +
                transformedRectangle[1] + "\n";
        String hAposDef = "Let h' be the orthogonal projection of p' onto segment " + transformedRectangle[3] +
                transformedRectangle[2] + ".\n";

        String sDef = "Let s be the orthogonal projection of " + transformedRectangle[0] + " onto the hintline of H3.\n";
        String sAposDef = "Let s' be the orthogonal projection of " + transformedRectangle[0] + " onto L1''.\n";
        String dDef = "Let d be the intersection between the hintline of H3 and " + transformedRectangle[1] +
                transformedRectangle[2] + ".\n";
        String dAposDef = "Let d' be the orthogonal projection of d onto line L1''.\n";

        String jDef = "Let j be the intersection of L1'' and segment  " + transformedRectangle[0] +
                transformedRectangle[1] + ".\n";
        String jAposDef = "Let j' be the orthogonal projeciton of j onto segment " + transformedRectangle[0] +
                transformedRectangle[3] + ".\n";

        String fDef = "Let f be the intersection of L1'' and the segment " + transformedRectangle[0]
                + transformedRectangle[1] + ".\n";
        String tDef = "Let t be the orthogonal projection of f to the opposite side of the previous rectangle.\n";

        // now the rectangles that are scanned, the rectangle the search rectangle is reduced to and the
        // definitions of the used points get joint so that one gets a coherent status message
        switch (caseIndex) {
            case 1:
                statusMessage = statusMessage.concat(L1DoubleAposDef + fDef + tDef +
                        "\n The search rectangle is reduced to f" + transformedRectangle[1] + transformedRectangle[2]
                        + "t.");
                break;
            case 2:
                statusMessage = statusMessage.concat(pDef + pAposDef + "\n" + mDef + kDef + mAposDef + kAposDef + "\n"
                        + gDef + hDef + "\n The rectangle m'k'km gets scanned and the search rectangle "
                        + "is reduced to g" + transformedRectangle[1] + transformedRectangle[2] + "h.");
                break;
            case 3:
                statusMessage = statusMessage.concat(L1DoubleAposDef + "\n" + sDef + sAposDef + dDef + dAposDef + "\n"
                        + pDef + "\n" + mDef + kDef + mAposDef + kAposDef + hDef + "\n The rectangles ss'd'd and m'k'km"
                        + " get scanned and the search rectangle is reduced to pk" + transformedRectangle[2] + "h."
                );
                break;
            case 4:
                statusMessage = statusMessage.concat(sDef + sAposDef + dDef + dAposDef + "\n" + pDef + "\n" + gDef +
                        hDef + gAposDef + hAposDef + "\n" + mDef + "\n The rectangles ss'd'd and gg'h'h get " +
                        "scanned and the search rectangle is reduced to " + transformedRectangle[0] + "gpm.");
                break;
            case 5:
                statusMessage = statusMessage.concat(pDef + "\n" + gDef + hDef + gAposDef + hAposDef + "\n" + mDef +
                        kDef + "\n The rectangle gg'h'h gets scanned and the search rectangle is reduced to " +
                        transformedRectangle[0] + transformedRectangle[1] + "km.");
                break;
            case 6:
                statusMessage = statusMessage.concat(L1DoubleAposDef + "\n" + jDef + jAposDef +
                        "\n The search rectangle is reduced to " + transformedRectangle[0] + transformedRectangle[1] +
                        "jj'.");
        }
        StatusMessageItem explanation = new StatusMessageItem(StatusMessageType.EXPLANATION_MOVEMENT, statusMessage);
        move.getStatusMessageItemsToBeAdded().add(explanation);
        strategy.statusMessageItemsToBeRemovedNextMove.add(explanation);
    }

    /**
     * If the last hint was bad, this function can be called and lastBadHint has to be set accordingly.
     * The function equals the "else"-part of the first if-condition in Algorithm 3 (Function ReduceRectangle(R))
     * in the paper.
     *
     * @param strategy
     * @param curHint
     * @param lastBadHint
     * @param move
     * @return The move to scan various areas so that A,B,C and D can be updated to a smaller rectangle (or the treasure
     * is found)
     */
    SearchPath lastHintBadSubroutine(StrategyFromPaper strategy, HalfPlaneHint curHint,
                                     HalfPlaneHint lastBadHint, SearchPath move) {

        try {
            initializeVariables(strategy, curHint, lastBadHint);
            int caseIndex = -1;

            // here begins line 24 of the ReduceRectangle routine from the paper:
            Coordinate[] newRectangle = null;

            if (x2Apos == right &&
                    lineBetweenClockwise(L2Apos, L1DoubleApos, ppApos)
            ) {
                caseIndex = 1;
                newRectangle = phiOtherRectangleInverse(basicTransformation, rect,
                        new Coordinate[]{f, B, C, t});
            }
            if (x2Apos == right &&
                    lineBetweenClockwise(L2Apos, ppApos, mAposKApos)
            ) {
                caseIndex = 2;
                move = rectangleScanPhiReverse(basicTransformation, rect, mApos, kApos, k, m, move);
                newRectangle = phiOtherRectangleInverse(basicTransformation, rect,
                        new Coordinate[]{g, B, C, h});
            }
            if ((x2Apos == left || x2Apos == down) &&
                    lineBetweenClockwise(L2Apos, mAposKApos, L1DoubleApos)
            ) {
                caseIndex = 3;
                // rectangleScan(phi_reverse(k, (s, s', d', d))
                move = rectangleScanPhiReverse(basicTransformation, rect, s, sApos, dApos, d, move);
                // rectangleScan(phi_reverse(k, (m', k', k, m))
                move = rectangleScanPhiReverse(basicTransformation, rect, mApos, kApos, k, m, move);
                // newRectangle := pkCh
                newRectangle = phiOtherRectangleInverse(basicTransformation, rect,
                        new Coordinate[]{p, k, C, h});
            }
            if (x2Apos == left &&
                    lineBetweenClockwise(L2Apos, L1DoubleApos, hAposGApos)
            ) {
                caseIndex = 4;
                // rectangleScan(phi_reverse(k, (s, s', d', d))
                move = rectangleScanPhiReverse(basicTransformation, rect, s, sApos, dApos, d, move);
                // rectangleScan(phi_reverse(k, (g, g', h', h))
                move = rectangleScanPhiReverse(basicTransformation, rect, g, gApos, h, hApos, move);
                // newRectangle := Agpm
                newRectangle = phiOtherRectangleInverse(basicTransformation, rect,
                        new Coordinate[]{A, g, p, m});
            }
            if ((x2Apos == left &&
                    lineBetweenClockwise(L2Apos, hAposGApos, ppApos)) ||
                    (x2Apos == left &&
                            lineBetweenClockwise(L2Apos, ppApos, mAposKApos)) ||
                    ((x2Apos == up || x2Apos == right) &&
                            lineBetweenClockwise(L2Apos, mAposKApos, pAposK)
                    )
            ) {
                caseIndex = 5;
                // rectangleScan(phireverse(k, (g, g', h', h))
                move = rectangleScanPhiReverse(basicTransformation, rect, g, gApos, hApos, h, move);
                // newRectangle := ABkm
                newRectangle = phiOtherRectangleInverse(basicTransformation, rect,
                        new Coordinate[]{A, B, k, m});
            }
            if (x2Apos == right &&
                    lineBetweenClockwise(L2Apos, pAposK, L1DoubleApos)
            ) {
                caseIndex = 6;
                // newRectangle := ABjj'
                newRectangle = phiOtherRectangleInverse(basicTransformation, rect,
                        new Coordinate[]{A, B, j, jApos});
            }
            addCaseDescriptionToStatus(move, basicTransformation, caseIndex, strategy);

            strategy.searchAreaCornerA = GEOMETRY_FACTORY.createPoint(newRectangle[0]);
            strategy.searchAreaCornerB = GEOMETRY_FACTORY.createPoint(newRectangle[1]);
            strategy.searchAreaCornerC = GEOMETRY_FACTORY.createPoint(newRectangle[2]);
            strategy.searchAreaCornerD = GEOMETRY_FACTORY.createPoint(newRectangle[3]);
            strategy.lastHintQuality = none;

            //move.addAdditionalItem(new GeometryItem<>(strategy.hintBeforePreviousHint.getHalfPlaneLineGeometry(),
            //GeometryType.HALF_PLANE_LINE_BROWN));

            return moveToCenterOfRectangle(strategy.searchAreaCornerA, strategy.searchAreaCornerB,
                    strategy.searchAreaCornerC, strategy.searchAreaCornerD, move);
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
    private boolean lineBetweenClockwise(LineSegment line, LineSegment between1, LineSegment between2) {
        LineSegment lineReverse = new LineSegment(line.p1, line.p0);
        LineSegment between2reverse = new LineSegment(between2.p1, between2.p0);
        double angleBetween1 = between1.angle();
        double maxAngleLineBetween1 = Math.max(normalizePositive(line.angle() - angleBetween1),
                normalizePositive(lineReverse.angle() - angleBetween1));
        double maxAngleBetween2and1 = Math.max(normalizePositive(between2.angle() - angleBetween1),
                normalizePositive(between2reverse.angle() - angleBetween1));
        if (maxAngleLineBetween1 == 0)
            return true;
        return maxAngleBetween2and1 < maxAngleLineBetween1;
    }

    private RuntimeException processError(Exception e, StrategyFromPaper s, Coordinate[] rect,
                                          HalfPlaneHint lastBadHint, HalfPlaneHint curHint) {
        Point A = s.searchAreaCornerA;
        Point B = s.searchAreaCornerB;
        Point C = s.searchAreaCornerC;
        Point D = s.searchAreaCornerD;
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
