package com.treasure.hunt.utils;

import com.treasure.hunt.strategy.hint.impl.AngleHint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static com.treasure.hunt.utils.JTSUtilsTestsUtils.eq;
import static com.treasure.hunt.utils.JTSUtilsTestsUtils.neq;

/**
 * Testbench, testing {@link JTSUtils#middleOfAngleHint(Coordinate, Coordinate, Coordinate)}.
 *
 * @author Dorian Reineccius
 */
class JTSUtilsMiddleOfAngleHintTest {
    private Coordinate C = new Coordinate(0, 0),
            N = new Coordinate(0, 1),
            NE = new Coordinate(1, 1),
            E = new Coordinate(1, 0),
            SE = new Coordinate(1, -1),
            S = new Coordinate(0, -1),
            SW = new Coordinate(-1, -1),
            W = new Coordinate(-1, 0),
            NW = new Coordinate(-1, 1);
    private Coordinate normNE = new Coordinate(Math.sqrt(0.5), Math.sqrt(0.5)),
            normSE = new Coordinate(Math.sqrt(0.5), -Math.sqrt(0.5)),
            normSW = new Coordinate(-Math.sqrt(0.5), -Math.sqrt(0.5)),
            normNW = new Coordinate(-Math.sqrt(0.5), Math.sqrt(0.5));

    @BeforeEach
    void shift() {
        double a = 1, b = 1;
        C.setX(C.x + a);
        C.setY(C.y + b);
        N.setX(N.x + a);
        N.setY(N.y + b);
        NE.setX(NE.x + a);
        NE.setY(NE.y + b);
        E.setX(E.x + a);
        E.setY(E.y + b);
        SE.setX(SE.x + a);
        SE.setY(SE.y + b);
        S.setX(S.x + a);
        S.setY(S.y + b);
        SW.setX(SW.x + a);
        SW.setY(SW.y + b);
        W.setX(W.x + a);
        W.setY(W.y + b);
        NW.setX(NW.x + a);
        NW.setY(NW.y + b);
        normNE.setX(normNE.x + a);
        normNE.setY(normNE.y + b);
        normSE.setX(normSE.x + a);
        normSE.setY(normSE.y + b);
        normSW.setX(normSW.x + a);
        normSW.setY(normSW.y + b);
        normNW.setX(normNW.x + a);
        normNW.setY(normNW.y + b);
    }

    /**
     * The 90Degrees tests open a 90 degree {@link AngleHint} and test,
     * whether {@link JTSUtils#middleOfAngleHint(Coordinate, Coordinate, Coordinate)} returns a point,
     * passing the middle of the AngleHint with a distance of 1.
     */
    @Test
    public void test90Degrees1() {
        eq(normNE, JTSUtils.middleOfAngleHint(E, C, N));
    }

    @Test
    public void test90Degrees2() {
        eq(E, JTSUtils.middleOfAngleHint(SE, C, NE));
    }

    @Test
    public void test90Degrees3() {
        eq(normSE, JTSUtils.middleOfAngleHint(S, C, E));
    }

    @Test
    public void test90Degrees4() {
        eq(S, JTSUtils.middleOfAngleHint(SW, C, SE));
    }

    @Test
    public void test90Degrees5() {
        eq(normSW, JTSUtils.middleOfAngleHint(W, C, S));
    }

    @Test
    public void test90Degrees6() {
        eq(W, JTSUtils.middleOfAngleHint(NW, C, SW));
    }

    @Test
    public void test90Degrees7() {
        eq(normNW, JTSUtils.middleOfAngleHint(N, C, W));
    }

    @Test
    public void test90Degrees8() {
        eq(N, JTSUtils.middleOfAngleHint(NE, C, NW));
    }

    /**
     * The 180Degrees tests open a 180 degree {@link AngleHint} and test,
     * whether {@link JTSUtils#middleOfAngleHint(Coordinate, Coordinate, Coordinate)} returns a point,
     * passing the middle of the AngleHint with a distance of 1.
     */
    @Test
    public void test180Degrees1() {
        eq(N, JTSUtils.middleOfAngleHint(E, C, W));
        neq(S, JTSUtils.middleOfAngleHint(E, C, W));
    }

    @Test
    public void test180Degrees2() {
        eq(normNE, JTSUtils.middleOfAngleHint(SE, C, NW));
        neq(normSW, JTSUtils.middleOfAngleHint(SE, C, NW));
    }

    @Test
    public void test180Degrees3() {
        eq(E, JTSUtils.middleOfAngleHint(S, C, N));
        neq(W, JTSUtils.middleOfAngleHint(S, C, N));
    }

    @Test
    public void test180Degrees4() {
        eq(normSE, JTSUtils.middleOfAngleHint(SW, C, NE));
        neq(normNW, JTSUtils.middleOfAngleHint(SW, C, NE));
    }

    @Test
    public void test180Degrees5() {
        eq(S, JTSUtils.middleOfAngleHint(W, C, E));
        neq(N, JTSUtils.middleOfAngleHint(W, C, E));
    }

    @Test
    public void test180Degrees6() {
        eq(normSW, JTSUtils.middleOfAngleHint(NW, C, SE));
        neq(normNE, JTSUtils.middleOfAngleHint(NW, C, SE));
    }

    @Test
    public void test180Degrees7() {
        eq(W, JTSUtils.middleOfAngleHint(N, C, S));
        neq(E, JTSUtils.middleOfAngleHint(N, C, S));
    }

    @Test
    public void test180Degrees8() {
        eq(normNW, JTSUtils.middleOfAngleHint(NE, C, SW));
        neq(normSE, JTSUtils.middleOfAngleHint(NE, C, SW));
    }

    /**
     * The 270Degrees tests open a 270 degree {@link AngleHint} and test,
     * whether {@link JTSUtils#middleOfAngleHint(Coordinate, Coordinate, Coordinate)} returns a point,
     * passing the middle of the AngleHint with a distance of 1.
     */
    @Test
    public void test270Degrees1() {
        eq(normSW, JTSUtils.middleOfAngleHint(N, C, E));
    }

    @Test
    public void test270Degrees2() {
        eq(W, JTSUtils.middleOfAngleHint(NE, C, SE));
    }

    @Test
    void test270Degrees3() {
        eq(normNW, JTSUtils.middleOfAngleHint(E, C, S));
    }

    @Test
    public void test270Degrees4() {
        eq(N, JTSUtils.middleOfAngleHint(SE, C, SW));
    }

    @Test
    public void test270Degrees5() {
        eq(normNE, JTSUtils.middleOfAngleHint(S, C, W));
    }

    @Test
    public void test270Degrees6() {
        eq(E, JTSUtils.middleOfAngleHint(SW, C, NW));
    }

    @Test
    public void test270Degrees7() {
        eq(normSE, JTSUtils.middleOfAngleHint(W, C, N));
    }

    @Test
    public void test270Degrees8() {
        eq(S, JTSUtils.middleOfAngleHint(NW, C, NE));
    }
}
