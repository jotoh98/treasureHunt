package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import java.util.List;

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
    void test() {
        Movement move = strat.move();
        List<GeometryItem<Point>> points = move.getPoints();
        Point[] true_points = new Point[]{
                createPoint(-1, 1), createPoint(-1, -1), createPoint(0, -1), createPoint(0, 1),
                createPoint(1, 1), createPoint(1, -1), createPoint(0, 0)
        };
        assertPoints(points, true_points);
    }

    void assertPoints(List<GeometryItem<Point>> strat, Point[] true_points) {
        assertTrue(strat.size() == true_points.length);
        for (int i = 0; i < true_points.length; i++) {
            System.out.println("Point " + i + " equals " + strat.get(i).getObject().toString() + " and should equal " +
                    true_points[i].toString());
            assertTrue(true_points[i].equalsExact(strat.get(i).getObject()));
        }

    }

}
