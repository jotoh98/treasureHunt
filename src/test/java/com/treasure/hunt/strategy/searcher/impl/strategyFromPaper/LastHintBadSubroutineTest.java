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
        strategy.lastHintBadSubroutine.lastHintBadSubroutine(currentHint, lastHint, move, true);
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
        strategy.lastHintBadSubroutine.lastHintBadSubroutine(currentHint, lastHint, move, true);
    }

    @Test
    void throwsError2() {
        strategy.searchAreaCornerA = JTSUtils.createPoint(-64, 64);
        strategy.searchAreaCornerB = JTSUtils.createPoint(-57.7396742, 64);
        strategy.searchAreaCornerC = JTSUtils.createPoint(-57.7396742, 0.2301608);
        strategy.searchAreaCornerD = JTSUtils.createPoint(-64, 0.2301608);
        HalfPlaneHint currentHint = new HalfPlaneHint(
                new Coordinate(-62.864965100519655, 32.25459294748826),
                new Coordinate(-62.864965100519655, 33.254592947488256));
        HalfPlaneHint lastHint = new HalfPlaneHint(
                new Coordinate(-60.869837, 32.11507999999999),
                new Coordinate(-60.80008052625587, 33.11264405025982));
        strategy.phase = 10;
        strategy.move(lastHint);
        SearchPath move = new SearchPath();
        strategy.lastHintBadSubroutine.lastHintBadSubroutine(currentHint, lastHint, move, true);
    }
}
