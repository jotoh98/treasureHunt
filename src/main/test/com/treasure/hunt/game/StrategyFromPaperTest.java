package com.treasure.hunt.game;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.strategy.searcher.implementations.StrategyFromPaper;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StrategyFromPaperTest {
    private StrategyFromPaper strat;

    @org.junit.jupiter.api.BeforeEach
    void setUp(){
        strat = new StrategyFromPaper();
        strat.init(JTSUtils.createPoint(0,0), new GameHistory());
    }

    @Test
    void test(){
        Moves mov  = strat.move();
        List<GeometryItem<Point>> points = mov.getPoints();
        //test if the rectangleScan works correct

    }

}
