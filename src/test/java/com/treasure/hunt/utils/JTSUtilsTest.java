package com.treasure.hunt.utils;

import com.treasure.hunt.strategy.hint.impl.AngleHint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testbench, testing {@link JTSUtils}.
 *
 * @author dorianreineccius
 */
class JTSUtilsTest {
    private Coordinate C = new Coordinate(0, 0),
            N = new Coordinate(0, 1),
            NE = new Coordinate(1, 1),
            E = new Coordinate(1, 0),
            SE = new Coordinate(1, -1),
            S = new Coordinate(0, -1),
            SW = new Coordinate(-1, -1),
            W = new Coordinate(-1, 0),
            NW = new Coordinate(-1, 1);
    private Coordinate normNE = new Coordinate(0.7071, 0.7071),
            normSE = new Coordinate(0.7071, -0.7071),
            normSW = new Coordinate(-0.7071, -0.7071),
            normNW = new Coordinate(-0.7071, 0.7071);

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
     * whether {@link JTSUtils#middleOfAngleHint(AngleHint)} returns a point,
     * passing the middle of the AngleHint with a distance of 1.
     */
    @Test
    void test90Degrees1() {
        eq(normNE, JTSUtils.middleOfAngleHint(createAngleHint(E, C, N)));
    }

    @Test
    void test90Degrees2() {
        eq(E, JTSUtils.middleOfAngleHint(createAngleHint(SE, C, NE)));
    }

    @Test
    void test90Degrees3() {
        eq(normSE, JTSUtils.middleOfAngleHint(createAngleHint(S, C, E)));
    }

    @Test
    void test90Degrees4() {
        eq(S, JTSUtils.middleOfAngleHint(createAngleHint(SW, C, SE)));
    }

    @Test
    void test90Degrees5() {
        eq(normSW, JTSUtils.middleOfAngleHint(createAngleHint(W, C, S)));
    }

    @Test
    void test90Degrees6() {
        eq(W, JTSUtils.middleOfAngleHint(createAngleHint(NW, C, SW)));
    }

    @Test
    void test90Degrees7() {
        eq(normNW, JTSUtils.middleOfAngleHint(createAngleHint(N, C, W)));
    }

    @Test
    void test90Degrees8() {
        eq(N, JTSUtils.middleOfAngleHint(createAngleHint(NE, C, NW)));
    }

    /**
     * The 180Degrees tests open a 180 degree {@link AngleHint} and test,
     * whether {@link JTSUtils#middleOfAngleHint(AngleHint)} returns a point,
     * passing the middle of the AngleHint with a distance of 1.
     */
    @Test
    void test180Degrees1() {
        eq(N, JTSUtils.middleOfAngleHint(createAngleHint(E, C, W)));
        neq(S, JTSUtils.middleOfAngleHint(createAngleHint(E, C, W)));
    }

    @Test
    void test180Degrees2() {
        eq(normNE, JTSUtils.middleOfAngleHint(createAngleHint(SE, C, NW)));
        neq(normSW, JTSUtils.middleOfAngleHint(createAngleHint(SE, C, NW)));
    }

    @Test
    void test180Degrees3() {
        eq(E, JTSUtils.middleOfAngleHint(createAngleHint(S, C, N)));
        neq(W, JTSUtils.middleOfAngleHint(createAngleHint(S, C, N)));
    }

    @Test
    void test180Degrees4() {
        eq(normSE, JTSUtils.middleOfAngleHint(createAngleHint(SW, C, NE)));
        neq(normNW, JTSUtils.middleOfAngleHint(createAngleHint(SW, C, NE)));
    }

    @Test
    void test180Degrees5() {
        eq(S, JTSUtils.middleOfAngleHint(createAngleHint(W, C, E)));
        neq(N, JTSUtils.middleOfAngleHint(createAngleHint(W, C, E)));
    }

    @Test
    void test180Degrees6() {
        eq(normSW, JTSUtils.middleOfAngleHint(createAngleHint(NW, C, SE)));
        neq(normNE, JTSUtils.middleOfAngleHint(createAngleHint(NW, C, SE)));
    }

    @Test
    void test180Degrees7() {
        eq(W, JTSUtils.middleOfAngleHint(createAngleHint(N, C, S)));
        neq(E, JTSUtils.middleOfAngleHint(createAngleHint(N, C, S)));
    }

    @Test
    void test180Degrees8() {
        eq(normNW, JTSUtils.middleOfAngleHint(createAngleHint(NE, C, SW)));
        neq(normSE, JTSUtils.middleOfAngleHint(createAngleHint(NE, C, SW)));
    }

    /**
     * The 270Degrees tests open a 270 degree {@link AngleHint} and test,
     * whether {@link JTSUtils#middleOfAngleHint(AngleHint)} returns a point,
     * passing the middle of the AngleHint with a distance of 1.
     */
    @Test
    void test270Degrees1() {
        eq(normSW, JTSUtils.middleOfAngleHint(createAngleHint(N, C, E)));
    }

    @Test
    void test270Degrees2() {
        eq(W, JTSUtils.middleOfAngleHint(createAngleHint(NE, C, SE)));
    }

    @Test
    void test270Degrees3() {
        eq(normNW, JTSUtils.middleOfAngleHint(createAngleHint(E, C, S)));
    }

    @Test
    void test270Degrees4() {
        eq(N, JTSUtils.middleOfAngleHint(createAngleHint(SE, C, SW)));
    }

    @Test
    void test270Degrees5() {
        eq(normNE, JTSUtils.middleOfAngleHint(createAngleHint(S, C, W)));
    }

    @Test
    void test270Degrees6() {
        eq(E, JTSUtils.middleOfAngleHint(createAngleHint(SW, C, NW)));
    }

    @Test
    void test270Degrees7() {
        eq(normSE, JTSUtils.middleOfAngleHint(createAngleHint(W, C, N)));
    }

    @Test
    void test270Degrees8() {
        eq(S, JTSUtils.middleOfAngleHint(createAngleHint(NW, C, NE)));
    }

    /**
     * @param c1 anglePointRight
     * @param c2 center
     * @param c3 anglePointLeft
     * @return {@link AngleHint} defined by c1, c2, c3
     */
    private AngleHint createAngleHint(Coordinate c1, Coordinate c2, Coordinate c3) {
        return new AngleHint(
                JTSUtils.createPoint(c1.x, c1.y),
                JTSUtils.createPoint(c2.x, c2.y),
                JTSUtils.createPoint(c3.x, c3.y));
    }

    /**
     * Tests, whether two {@link Coordinate}'s are approximately equal.
     *
     * @param expected {@link Coordinate}
     * @param actual   {@link Coordinate}
     */
    private void eq(Coordinate expected, Coordinate actual) {
        assertTrue(Math.abs(expected.x - actual.x) < 0.0001 && Math.abs(expected.y - actual.y) < 0.0001,
                "Expected: x≈" + expected.x + " and y≈" + expected.y + ", but was ≈(" + actual.x + ", " + actual.y + ").");
    }

    /**
     * Tests, whether two {@link Coordinate}'s are approximately unequal.
     *
     * @param expected {@link Coordinate}
     * @param actual   {@link Coordinate}
     */
    private void neq(Coordinate expected, Coordinate actual) {
        assertTrue(Math.abs(expected.x - actual.x) > 0.0001 || Math.abs(expected.y - actual.y) > 0.0001,
                "Expected: x≠" + expected.x + " or y≠" + expected.y + ", but was ≈(" + actual.x + ", " + actual.y + ").");
    }
}
