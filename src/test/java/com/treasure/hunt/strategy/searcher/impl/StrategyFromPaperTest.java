package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.StrategyFromPaper;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.Tester;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.utils.JTSUtils.createPoint;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StrategyFromPaperTest {
    private StrategyFromPaper strat;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        strat = new StrategyFromPaper();
        strat.init(JTSUtils.createPoint(0, 0));
    }

    void assertPoints(List<GeometryItem<Point>> strat_points, Point[] true_points) {
        assertTrue(strat_points.size() == true_points.length,
                "Length of moves equals " + strat_points.size() + " and should equal " + true_points.length);
        for (int i = 0; i < true_points.length; i++) {
            assertTrue(true_points[i].equalsExact(strat_points.get(i).getObject(), 0.000000000000001),
                    "Point " + i + " equals " + strat_points.get(i).getObject().toString() + " and should equal " +
                            true_points[i].toString());
        }
    }

    @Test
    void moveOnce() {
        Movement move = strat.move();
        List<GeometryItem<Point>> movePoints = move.getPoints();
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
        List<GeometryItem<Point>> movePoints = strat.move(testedHint).getPoints();
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
        HalfPlaneHint testedHint = new HalfPlaneHint(new Coordinate(-4, 3), new Coordinate(4, -3), left);
        List<GeometryItem<Point>> movePoints = strat.move(testedHint).getPoints();
        Point[] correctMovePoints = new Point[]{createPoint(0, 0), createPoint(0, -0.5)};
        assertPoints(movePoints, correctMovePoints);

        testedHint = new HalfPlaneHint(new Coordinate(2.5, 3), new Coordinate(-2.5, -4), right);
        movePoints = strat.move(testedHint).getPoints();
        correctMovePoints = new Point[]{createPoint(0, -0.5), createPoint(0.75, -0.5)};
        assertPoints(movePoints, correctMovePoints);

        testedHint = new HalfPlaneHint(new Coordinate(-4, -0.5), new Coordinate(4, -0.5), up);
        movePoints = strat.move(testedHint).getPoints();
        correctMovePoints = new Point[]{createPoint(0.75, -0.5), createPoint(0.75, 1.25)};
        assertPoints(movePoints, correctMovePoints);
    }

    @Test
    void moveBadHint() {
        moveOnce();
        HalfPlaneHint hint = new HalfPlaneHint(new Coordinate(0, 0),
                new Coordinate(0.8269335876981098, -0.5622996012240562));
        List<GeometryItem<Point>> points = strat.move(hint).getPoints();
        Point[] true_points = new Point[]{createPoint(0, 0),
                createPoint(1.1245992024481124, 1.6538671753962195)};
        hint = new HalfPlaneHint(new Coordinate(1.1245992024481124, 1.6538671753962195),
                new Coordinate(1.2148090667719662, 0.6579443970751934));
        strat.move(hint);
    }

    @Test
    void testStrategyFromPaperInternMethods() {
        Tester tester = new Tester(strat);
        tester.testBasicTransformation();
        tester.testPhiHint();
        tester.testPhiRectangle();
        tester.testPhiRectangleRectangleReverse();
    }
}
