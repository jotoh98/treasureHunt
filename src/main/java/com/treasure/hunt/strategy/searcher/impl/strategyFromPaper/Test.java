package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;

import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.BadHintSubroutine.lastHintBadSubroutine;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper.*;
import static com.treasure.hunt.utils.JTSUtils.*;

/**
 * @author bsen
 */
public class Test {
    StrategyFromPaper strategy;

    public Test(StrategyFromPaper strategy) {
        this.strategy = strategy;
    }

    private void testRectHint(Coordinate[] rect, HalfPlaneHint hint, int basicTrans) {
        int testBasicTrans = getBasicTransformation(rect, hint);
        if (basicTrans != testBasicTrans) {
            throw new IllegalArgumentException("The basic transformation should equal " + basicTrans +
                    " but equals " + testBasicTrans);
        }
    }

    private void testLastHintBadSubroutine(StrategyFromPaper strategy, Coordinate[] rect, HalfPlaneHint lastBadHint,
                                           HalfPlaneHint curHint) {
        strategy.A = GEOMETRY_FACTORY.createPoint(rect[0]);
        strategy.B = GEOMETRY_FACTORY.createPoint(rect[1]);
        strategy.C = GEOMETRY_FACTORY.createPoint(rect[2]);
        strategy.D = GEOMETRY_FACTORY.createPoint(rect[3]);
        strategy.lastBadHint = lastBadHint;
        lastHintBadSubroutine(strategy, curHint, lastBadHint, new Movement());
    }

    public void testBadCases() {

        Coordinate[] rect = new Coordinate[]{new Coordinate(-4, 4), new Coordinate(4, 4),
                new Coordinate(4, -4), new Coordinate(-4, -4)};
        HalfPlaneHint lastBadHint = new HalfPlaneHint(new Coordinate(0, 0),
                new Coordinate(0.7377637010688854, -0.675059050294965));
        HalfPlaneHint curHint = new HalfPlaneHint(new Coordinate(1.3501181005899303, 1.4755274021377711),
                new Coordinate(2.3366624680024213, 1.3120336373432429));
        //should be case five
        testRectHint(rect, lastBadHint, 0);
        strategy.A = createPoint(-2, 2);
        strategy.B = createPoint(2, 2);
        strategy.C = createPoint(2, -2);
        strategy.D = createPoint(-2, -2);
        strategy.lastBadHint = lastBadHint;
        lastHintBadSubroutine(strategy, curHint, lastBadHint, new Movement());

        HalfPlaneHint hint = new HalfPlaneHint(new Coordinate(0, 0),
                new Coordinate(0.6209474701786085, 0.7838521794820666));
        testRectHint(rect, hint, 3);
        rect = new Coordinate[]{new Coordinate(-2, 2), new Coordinate(2, 2),
                new Coordinate(2, -2), new Coordinate(-2, -2)};
        hint = new HalfPlaneHint(new Coordinate(0, 0),
                new Coordinate(0.7416025214414383, 0.6708395487683333));
        testRectHint(rect, hint, 4);

        //badCase0
        rect = new Coordinate[]{
                new Coordinate(-2.159168821737699, 8.0),
                new Coordinate(8.0, 8.0),
                new Coordinate(8.0, -3.999532170942503),
                new Coordinate(-2.159168821737699, -3.999532170942503)
        };
        lastBadHint = new HalfPlaneHint(new Coordinate(2.9204156, 2.0002339),
                new Coordinate(3.5662858179937924, 2.7636811224775273));
        curHint = new HalfPlaneHint(new Coordinate(1.3935211550449453, 3.291974335987585),
                new Coordinate(2.3662835676900604, 3.060169917253051));
        testRectHint(rect, lastBadHint, 3);
        testLastHintBadSubroutine(strategy, rect, lastBadHint, curHint);

        //badCase0 transformed so that basicTrans is 0
        rect = new Coordinate[]{
                new Coordinate(-3.999532170942503, 2.3662835676900604),
                new Coordinate(8.0, 2.3662835676900604),
                new Coordinate(8.0, -8.0),
                new Coordinate(-3.999532170942503, -8.0)
        };
        lastBadHint = new HalfPlaneHint(new Coordinate(2.9204156, 2.0002339),
                new Coordinate(3.291974335987585, -1.3935211550449453));
        curHint = new HalfPlaneHint(new Coordinate(2.7636811224775273, -3.5662858179937924),
                new Coordinate(3.060169917253051, -2.3662835676900604));
        //testRectHint(rect, lastBadHint, 0); //TODO evtl diesen test rauswerfen
        //testLastHintBadSubroutine(strategy, rect, lastBadHint, curHint);
    }

    public void testPhiRectangle() {
        Coordinate[] rect = new Coordinate[]{new Coordinate(-4, 4), new Coordinate(4, 4),
                new Coordinate(4, -4), new Coordinate(-4, -4)};
        Coordinate[] testRect = phiRectangle(3, rect);
        if (!doubleEqual(testRect[0].x, -4) || !doubleEqual(testRect[0].y, 4) ||
                !doubleEqual(testRect[1].x, 4) || !doubleEqual(testRect[1].y, 4) ||
                !doubleEqual(testRect[2].x, 4) || !doubleEqual(testRect[2].y, -4) ||
                !doubleEqual(testRect[3].x, -4) || !doubleEqual(testRect[3].y, -4)) {
            throw new IllegalArgumentException(Arrays.toString(testRect));
        }
    }

    public void testPhiHint() {
        Coordinate[] rect = new Coordinate[]{new Coordinate(-4, 4), new Coordinate(4, 4),
                new Coordinate(4, -4), new Coordinate(-4, -4)};
        HalfPlaneHint hint = new HalfPlaneHint(new Coordinate(0, 0),
                new Coordinate(0.6209474701786085, 0.7838521794820666));
        HalfPlaneHint testHint = phiHint(3, rect, hint);
        if (!doubleEqual(testHint.getRightPoint().getX(), 0.7838521794820666) ||
                !doubleEqual(testHint.getRightPoint().getY(), -0.6209474701786085)) {
            throw new IllegalArgumentException("right angle point is " + testHint.getRightPoint() +
                    " and should equal (0.7838521794820666, -0.6209474701786085)");
        }
        if (!doubleEqual(testHint.getLeftPoint().getX(), 0) ||
                !doubleEqual(testHint.getLeftPoint().getY(), 0)) {
            throw new IllegalArgumentException("left angle point is " + testHint.getLeftPoint() +
                    " and should equal (0.0, 0.0)");
        }
    }
}

