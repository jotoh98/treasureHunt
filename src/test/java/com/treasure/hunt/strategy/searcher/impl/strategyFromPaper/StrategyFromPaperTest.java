package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import java.util.List;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.utils.JTSUtils.GEOMETRY_FACTORY;
import static com.treasure.hunt.utils.JTSUtils.createPoint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Rank
 */

public class StrategyFromPaperTest {
    private StrategyFromPaper strat;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        strat = new StrategyFromPaper();
        strat.init(JTSUtils.createPoint(0, 0));
    }

    void assertPoints(List<Point> strategyPoints, Point[] truePoints) {
        assertEquals(strategyPoints.size(), truePoints.length, "Length of moves equals " +
                strategyPoints.size() + " and should equal " + truePoints.length);

        for (int i = 0; i < truePoints.length; i++) {
            assertTrue(truePoints[i].equalsExact(strategyPoints.get(i), 0.0000001),
                    "Point " + i + " equals " + strategyPoints.get(i).toString() + " and should equal " +
                            truePoints[i].toString());
        }
    }

    @Test
    void moveOnce() {
        SearchPath move = strat.move();
        List<Point> movePoints = move.getPoints();
        Point[] correctMovePoints = new Point[]{
                createPoint(-1, 1), createPoint(-1, -1), createPoint(0, -1),
                createPoint(0, 1), createPoint(1, 1), createPoint(1, -1), createPoint(0, 0)
        };
        assertPoints(movePoints, correctMovePoints);
    }

    //@Test
    void moveTwice() {// todo redo
        moveOnce();
        HalfPlaneHint testedHint = new HalfPlaneHint(new Coordinate(-2, 1), new Coordinate(2, -1), right);
        List<Point> movePoints = strat.move(testedHint).getPoints();
        assertPoints(movePoints, new Point[]{createPoint(0, 0.5)});

        testedHint = new HalfPlaneHint(new Coordinate(-2, 0.5), new Coordinate(2, 0.5), down);
        movePoints = strat.move(testedHint).getPoints();
        Point[] correctMovePoints = new Point[]{
                createPoint(-2, 2), createPoint(-2, -1), createPoint(-1, -1), createPoint(-1, 2),
                createPoint(0, 2), createPoint(0, -1), createPoint(1, -1), createPoint(1, 2),
                createPoint(2, 2), createPoint(2, -1),

                createPoint(0, 0)
        };
        assertPoints(movePoints, correctMovePoints);
    }

    void moveUntilThirdRectangle() {//todo redo
        moveTwice();
        HalfPlaneHint testHint = new HalfPlaneHint(new Coordinate(-4, 3), new Coordinate(4, -3), left);
        List<Point> movePoints = strat.move(testHint).getPoints();
        Point[] correctMovePoints = new Point[]{createPoint(0, -0.5)};
        assertPoints(movePoints, correctMovePoints);

        testHint = new HalfPlaneHint(new Coordinate(2.5, 3), new Coordinate(-2.5, -4), right);
        movePoints = strat.move(testHint).getPoints();
        correctMovePoints = new Point[]{createPoint(0.75, -0.5)};
        assertPoints(movePoints, correctMovePoints);

        testHint = new HalfPlaneHint(new Coordinate(-4, -0.5), new Coordinate(4, -0.5), up);
        movePoints = strat.move(testHint).getPoints();
        correctMovePoints = new Point[]{createPoint(0.75, 1.25)};
        assertPoints(movePoints, correctMovePoints);
    }

    @Test
    void moveBadHint() {
        moveOnce();

        HalfPlaneHint hint;
        hint = new HalfPlaneHint(new Coordinate(1.1245992024481124, 1.6538671753962195),
                new Coordinate(1.2148090667719662, 0.6579443970751934));
        strat.move(hint);
    }

    /**
     * myhint is HalfPlaneHint((0,0), (4,-3.5))
     *
     * @return
     */
    Coordinate getPAposForMyBadHint() {
        Vector2D intersectionHintAD = new Vector2D(-4, 3.5);
        double lengthCenterToIntersectionHintAD = Math.sqrt(Math.pow(4, 2) + Math.pow(3.5, 2));
        Vector2D pAposPlusHalfPiAngle = intersectionHintAD.divide(lengthCenterToIntersectionHintAD / 2);
        Vector2D pAposVector = pAposPlusHalfPiAngle.rotateByQuarterCircle(3);
        Coordinate pApos = pAposVector.toCoordinate();
        return pApos;
    }

    LineSegment getL1DoubleAposForMyBadHint() {
        Coordinate pApos = getPAposForMyBadHint();
        return new LineSegment(pApos, new Coordinate(-4 + pApos.x, 3.5 + pApos.y));
    }

    Coordinate getFForMyBadHint() {
        LineSegment AB = new LineSegment(strat.searchAreaCornerA.getCoordinate(),
                strat.searchAreaCornerB.getCoordinate());
        return getL1DoubleAposForMyBadHint().lineIntersection(AB);
    }

    Coordinate getTForMyBadHint() {
        Coordinate f = getFForMyBadHint();
        return new Coordinate(f.x, -4);
    }

    Coordinate getMForMyBadHint() {
        return new Coordinate(-4, 0);
    }

    Coordinate getMAposForMyBadHint() {
        return new Coordinate(-4, getPAposForMyBadHint().y);
    }

    Coordinate getKForMyBadHint() {
        return new Coordinate(4, 0);
    }

    Coordinate getKAposForMyBadHint() {
        return new Coordinate(4, getPAposForMyBadHint().y);
    }

    Coordinate getGForMyBadHint() {
        return new Coordinate(0, 4);
    }

    Coordinate getGAposForMyBadHint() {
        return new Coordinate(getPAposForMyBadHint().x, 4);
    }

    Coordinate getHForMyBadHint() {
        return new Coordinate(0, -4);
    }

    Coordinate getHAposForMyBadHint() {
        return new Coordinate(getPAposForMyBadHint().x, -4);
    }

    Coordinate getSForMyBadHint() {
        Coordinate a = new Coordinate(-4, 4); // The left top point of the current rectangle
        LineSegment L1Apos = new LineSegment(new Coordinate(0, 0), new Coordinate(4, -3.5));
        double distancePToA = new Coordinate(0, 0).distance(a);
        double distanceSToA = L1Apos.distancePerpendicular(a);
        double distancePToS = Math.sqrt(Math.pow(distancePToA, 2) - Math.pow(distanceSToA, 2));
        Vector2D pToS = new Vector2D(L1Apos.p1, L1Apos.p0).multiply(distancePToS / L1Apos.getLength());
        return pToS.toCoordinate();
    }

    Coordinate getSAposForMyBadHint() {
        return new Coordinate(getSForMyBadHint().x + getPAposForMyBadHint().x,
                getSForMyBadHint().y + getPAposForMyBadHint().y);
    }

    Coordinate getDForMyBadHint() {
        return new Coordinate(4, -3.5);
    }

    Coordinate getDAposForMyBadHint() {
        return new Coordinate(getDForMyBadHint().x + getPAposForMyBadHint().x,
                getDForMyBadHint().y + getPAposForMyBadHint().y);
    }

    Coordinate getJForMyBadHint() {
        LineSegment bc = new LineSegment(new Coordinate(4, 4), new Coordinate(4, -4));
        return JTSUtils.lineWayIntersection(getL1DoubleAposForMyBadHint(), bc);
    }

    @Test
    void oneBadHint() {
        strat.phase = 3;
        strat.searchAreaCornerA = JTSUtils.createPoint(-4, 4);
        strat.searchAreaCornerB = JTSUtils.createPoint(4, 4);
        strat.searchAreaCornerC = JTSUtils.createPoint(4, -4);
        strat.searchAreaCornerD = JTSUtils.createPoint(-4, -4);
        HalfPlaneHint firstHint = new HalfPlaneHint(new Coordinate(0, 0), new Coordinate(4, -3.5));
        SearchPath searcherMove = strat.move(firstHint);

        Point[] correctPoints = new Point[]{GEOMETRY_FACTORY.createPoint(getPAposForMyBadHint())};
        assertPoints(searcherMove.getPoints(), correctPoints);
    }

    @Test
    void lastHintBadCaseOneTest() {
        oneBadHint();
        HalfPlaneHint secondHint = new HalfPlaneHint(getPAposForMyBadHint(), new Coordinate(-0.5, -4));
        SearchPath searcherMove = strat.move(secondHint);
        Coordinate centerOfNewRectangle = new Coordinate((4 + getFForMyBadHint().x) / 2, 0);
        Point[] correctPoints = new Point[]{GEOMETRY_FACTORY.createPoint(centerOfNewRectangle)};
        assertPoints(searcherMove.getPoints(), correctPoints);
    }

    @Test
    void lastHintBadCaseTwoTest() {
        oneBadHint();
        HalfPlaneHint secondHint = new HalfPlaneHint(getPAposForMyBadHint(), new Coordinate(-4, 1));
        SearchPath searcherMove = strat.move(secondHint);
        Point[] correctPoints = new Point[]{
                //scan m'k'km:
                GEOMETRY_FACTORY.createPoint(getMForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getKForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getKAposForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getMAposForMyBadHint()),
                // go to the center of the new rectangle
                JTSUtils.createPoint(2, 0)
        };
        assertPoints(searcherMove.getPoints(), correctPoints);
    }

    @Test
    void lastHintBadCaseThreeTest() {
        oneBadHint();
        HalfPlaneHint secondHint = new HalfPlaneHint(getPAposForMyBadHint(), new Coordinate(-4, 3));
        SearchPath searcherMove = strat.move(secondHint);
        Point[] correctPoints = new Point[]{
                //scan ss'd'd
                GEOMETRY_FACTORY.createPoint(getSForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getDForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getDAposForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getSAposForMyBadHint()),
                //scan m'k'km
                GEOMETRY_FACTORY.createPoint(getMForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getKForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getKAposForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getMAposForMyBadHint()),
                // go to the center of the new rectangle
                JTSUtils.createPoint(2, -2)
        };
        assertPoints(searcherMove.getPoints(), correctPoints);
    }

    @Test
    void lastHintBadCaseFourTest() {
        oneBadHint();
        HalfPlaneHint secondHint = new HalfPlaneHint(getPAposForMyBadHint(), new Coordinate(-0.5, 4));
        SearchPath searcherMove = strat.move(secondHint);
        Point[] correctPoints = new Point[]{
                //scan ss'd'd
                GEOMETRY_FACTORY.createPoint(getSForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getDForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getDAposForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getSAposForMyBadHint()),
                //scan gg'h'h
                GEOMETRY_FACTORY.createPoint(getGForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getHForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getHAposForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getGAposForMyBadHint()),
                // go to the center of the new rectangle
                JTSUtils.createPoint(-2, 2)
        };
        assertPoints(searcherMove.getPoints(), correctPoints);
    }

    void assertCaseFive(SearchPath searcherMove) {
        Point[] correctPoints = new Point[]{
                //scan gg'h'h
                GEOMETRY_FACTORY.createPoint(getGForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getHForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getHAposForMyBadHint()),
                GEOMETRY_FACTORY.createPoint(getGAposForMyBadHint()),
                // go to the center of the new rectangle
                JTSUtils.createPoint(0, 2)
        };
        assertPoints(searcherMove.getPoints(), correctPoints);
    }

    @Test
    void lastHintBadCaseFiveTest1() {
        oneBadHint();
        HalfPlaneHint secondHint = new HalfPlaneHint(getPAposForMyBadHint(), new Coordinate(2.5, 4));
        SearchPath searcherMove = strat.move(secondHint);
        assertCaseFive(searcherMove);
    }

    @Test
    void lastHintBadCaseFiveTest2() {
        oneBadHint();
        HalfPlaneHint secondHint = new HalfPlaneHint(getPAposForMyBadHint(), new Coordinate(4, 3));
        SearchPath searcherMove = strat.move(secondHint);
        assertCaseFive(searcherMove);
    }

    @Test
    void lastHintBadCaseFiveTest3() {
        oneBadHint();
        HalfPlaneHint secondHint = new HalfPlaneHint(getPAposForMyBadHint(), new Coordinate(4, 0.5));
        SearchPath searcherMove = strat.move(secondHint);
        assertCaseFive(searcherMove);
    }

    @Test
    void lastHintBadCaseSixTest() {
        oneBadHint();
        HalfPlaneHint secondHint = new HalfPlaneHint(getPAposForMyBadHint(), new Coordinate(4, -0.5));
        SearchPath searcherMove = strat.move(secondHint);
        Point[] correctPoints = new Point[]{JTSUtils.createPoint(0, (getJForMyBadHint().y + 4) / 2)};
        assertPoints(searcherMove.getPoints(), correctPoints);
    }
}
