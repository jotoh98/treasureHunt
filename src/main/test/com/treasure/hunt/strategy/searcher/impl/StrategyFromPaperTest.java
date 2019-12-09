package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.Test;
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

    @Test
    void moveOnce() {
        Movement move = strat.move();
        List<GeometryItem<Point>> points = move.getPoints();
        Point[] true_points = new Point[]{
                createPoint(-1, 1), createPoint(-1, -1), createPoint(0, -1), createPoint(0, 1),
                createPoint(1, 1), createPoint(1, -1), createPoint(0, 0)
        };
        assertPoints(points, true_points);
    }

    void assertPoints(List<GeometryItem<Point>> strat, Point[] true_points) {
        assertTrue(strat.size() == true_points.length,
                "Length of moves equals " + strat.size() + " and should equal " + true_points.length);
        for (int i = 0; i < true_points.length; i++) {
            assertTrue(true_points[i].equalsExact(strat.get(i).getObject(), 0.000000000000001),
                    "Point " + i + " equals " + strat.get(i).getObject().toString() + " and should equal " +
                            true_points[i].toString());
        }
    }

    @Test
    void moveTwice() {
        moveOnce();
        HalfPlaneHint hint = new HalfPlaneHint(createPoint(-2, 1), createPoint(2, -1), right);
        List<GeometryItem<Point>> points = strat.move(hint).getPoints();
        assertPoints(points, new Point[]{createPoint(0, 0.5)});

        hint = new HalfPlaneHint(createPoint(-2, 0.5), createPoint(2, 0.5), down);
        points = strat.move(hint).getPoints();
        Point[] true_points = new Point[]{
                createPoint(-2, 2), createPoint(-2, -1), createPoint(-1, -1), createPoint(-1, 2),
                createPoint(0, 2), createPoint(0, -1), createPoint(1, -1), createPoint(1, 2),
                createPoint(2, 2), createPoint(2, -1), createPoint(0, 0)
        };
        assertPoints(points, true_points);
    }

    @Test
    void moveUntilThirdRectangle() {
        moveTwice();
        HalfPlaneHint hint = new HalfPlaneHint(createPoint(-4, 3), createPoint(4, -3), left);
        List<GeometryItem<Point>> points = strat.move(hint).getPoints();
        Point[] true_points = new Point[]{createPoint(0, -0.5)};
        assertPoints(points, true_points);

        hint = new HalfPlaneHint(createPoint(2.5, 3), createPoint(-2.5, -4), right);
        points = strat.move(hint).getPoints();
        true_points = new Point[]{createPoint(0.75, -0.5)};
        assertPoints(points, true_points);

        hint = new HalfPlaneHint(createPoint(-4, -0.5), createPoint(4, -0.5), up);
        points = strat.move(hint).getPoints();
        true_points = new Point[]{createPoint(0.75, 1.25)};
        assertPoints(points, true_points);

        hint = new HalfPlaneHint(createPoint(-2.5, 3), createPoint(4, -0.5), right);
        points = strat.move(hint).getPoints();
        for (int i = 0; i < points.size(); i++) {
            System.out.print(points.get(i).getObject());
        }
        System.out.println();
    }
}
