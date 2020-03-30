package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

/**
 * @author Rank
 */
public class LastHintBadSubroutineTest {
    private StrategyFromPaper strategy;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        strategy = new StrategyFromPaper();
        strategy.init(JTSUtils.createPoint(0, 0));
    }

    @Test
    void throwsError0() {
        strategy.searchAreaCornerA = JTSUtils.createPoint(-4, 2.12683773);
        strategy.searchAreaCornerB = JTSUtils.createPoint(4, 2.12683773);
        strategy.searchAreaCornerC = JTSUtils.createPoint(4, -4);
        strategy.searchAreaCornerD = JTSUtils.createPoint(-4, -4);

        HalfPlaneHint currentHint =
                new HalfPlaneHint(new Coordinate(-0.9999999999999999, -2.6686319075688774),
                        new Coordinate(-1.8660254037844386, -2.1686319075688774));
        HalfPlaneHint lastHint =
                new HalfPlaneHint(new Coordinate(0.0, -0.9365811),
                        new Coordinate(-0.8660254037844387, -0.4365811000000001));
        strategy.phase = 4;
        strategy.move(lastHint);
        SearchPath move = new SearchPath();
        strategy.lastHintBadSubroutine.lastHintBadSubroutine(currentHint, lastHint, move);
    }

    @Test
    void throwsError1() {
        strategy.searchAreaCornerA = JTSUtils.createPoint(-128, 23.4628483);
        strategy.searchAreaCornerB = JTSUtils.createPoint(128, 23.4628483);
        strategy.searchAreaCornerC = JTSUtils.createPoint(128, 18.887831);
        strategy.searchAreaCornerD = JTSUtils.createPoint(-128, 18.887831);
        HalfPlaneHint currentHint = new HalfPlaneHint(
                new Coordinate(-0.03490481287456504, 19.175644609687215),
                new Coordinate(0.965095187125435, 19.175644609687215)
        );
        HalfPlaneHint lastHint = new HalfPlaneHint(
                new Coordinate(0.0, 21.17534), new Coordinate(-0.9998476951563913, 21.19279240643728)
        );
        strategy.phase = 10;
        strategy.move(lastHint);
        SearchPath move = new SearchPath();
        strategy.lastHintBadSubroutine.lastHintBadSubroutine(currentHint, lastHint, move);
    }
}
