package com.treasure.hunt.utils;

import com.treasure.hunt.strategy.hint.impl.AngleHint;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private AngleHint createAngleHint(Coordinate c1, Coordinate c2, Coordinate c3) {
        return new AngleHint(
                JTSUtils.createPoint(c1.x, c1.y),
                JTSUtils.createPoint(c2.x, c2.y),
                JTSUtils.createPoint(c3.x, c3.y));
    }

    private void eq(Coordinate expected, Coordinate actual) {
        assertTrue(Math.abs(expected.x - actual.x) < 0.0001 && Math.abs(expected.y - actual.y) < 0.0001,
                "Expected: x≈" + expected.x + " and y≈" + expected.y + ", but was ≈(" + actual.x + ", " + actual.y + ").");
    }

    private void neq(Coordinate expected, Coordinate actual) {
        assertTrue(Math.abs(expected.x - actual.x) > 0.0001 || Math.abs(expected.y - actual.y) > 0.0001,
                "Expected: x≠" + expected.x + " or y≠" + expected.y + ", but was ≈(" + actual.x + ", " + actual.y + ").");
    }
}
