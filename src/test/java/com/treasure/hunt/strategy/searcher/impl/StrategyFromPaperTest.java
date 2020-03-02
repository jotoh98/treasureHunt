package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.StrategyFromPaper;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.utils.JTSUtils.createPoint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StrategyFromPaperTest {
    private StrategyFromPaper strat;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        strat = new StrategyFromPaper();
        strat.init(JTSUtils.createPoint(0, 0));
    }

    void assertPoints(List<Point> stratPoints, Point[] truePoints) {
        assertEquals(stratPoints.size(), truePoints.length, "Length of moves equals " + stratPoints.size() + " and should equal " + truePoints.length);
        for (int i = 0; i < truePoints.length; i++) {
            assertTrue(truePoints[i].equalsExact(stratPoints.get(i), 0.000000000000001),
                    "Point " + i + " equals " + stratPoints.get(i).toString() + " and should equal " +
                            truePoints[i].toString());
        }
    }

    @Test
    void moveOnce() {
        SearchPath move = strat.move();
        List<Point> movePoints = move.getPoints();
        Point[] correctMovePoints = new Point[]{
                createPoint(0, 0), createPoint(-1, 1), createPoint(-1, -1), createPoint(0, -1),
                createPoint(0, 1), createPoint(1, 1), createPoint(1, -1), createPoint(0, 0)
        };
        assertPoints(movePoints, correctMovePoints);
    }

    @Test
    void moveTwice() {
        moveOnce();
        HalfPlaneHint testedHint = new HalfPlaneHint(new Coordinate(-2, 1), new Coordinate(2, -1), right);
        List<Point> movePoints = strat.move(testedHint).getPoints();
        assertPoints(movePoints, new Point[]{createPoint(0, 0), createPoint(0, 0.5)});

        testedHint = new HalfPlaneHint(new Coordinate(-2, 0.5), new Coordinate(2, 0.5), down);
        movePoints = strat.move(testedHint).getPoints();
        Point[] correctMovePoints = new Point[]{
                createPoint(0, 0.5),
                createPoint(-2, 2), createPoint(-2, -1), createPoint(-1, -1), createPoint(-1, 2),
                createPoint(0, 2), createPoint(0, -1), createPoint(1, -1), createPoint(1, 2),
                createPoint(2, 2), createPoint(2, -1), createPoint(0, 0)
        };
        assertPoints(movePoints, correctMovePoints);
    }

    @Test
    void moveUntilThirdRectangle() {
        moveTwice();
        HalfPlaneHint testHint = new HalfPlaneHint(new Coordinate(-4, 3), new Coordinate(4, -3), left);
        List<Point> movePoints = strat.move(testHint).getPoints();
        Point[] correctMovePoints = new Point[]{createPoint(0, 0), createPoint(0, -0.5)};
        assertPoints(movePoints, correctMovePoints);

        testHint = new HalfPlaneHint(new Coordinate(2.5, 3), new Coordinate(-2.5, -4), right);
        movePoints = strat.move(testHint).getPoints();
        correctMovePoints = new Point[]{createPoint(0, -0.5), createPoint(0.75, -0.5)};
        assertPoints(movePoints, correctMovePoints);

        testHint = new HalfPlaneHint(new Coordinate(-4, -0.5), new Coordinate(4, -0.5), up);
        movePoints = strat.move(testHint).getPoints();
        correctMovePoints = new Point[]{createPoint(0.75, -0.5), createPoint(0.75, 1.25)};
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
}
