package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPathPrototype;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;

import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper.*;
import static com.treasure.hunt.utils.JTSUtils.doubleEqual;

public class RoutinesFromPaperTest {

    @BeforeEach
    void setUp(){

    }

    private void testRectHint(Coordinate[] rect, HalfPlaneHint hint, int basicTrans) {
        int testBasicTrans = getBasicTransformation(rect, hint);
        if (basicTrans != testBasicTrans) {
            throw new IllegalArgumentException("The basic transformation should equal " + basicTrans +
                    " but equals " + testBasicTrans);
        }
    }

    @Test
    public void testBasicTransformation() {
        Coordinate[] rect = new Coordinate[]{new Coordinate(-4, 4), new Coordinate(4, 4),
                new Coordinate(4, -4), new Coordinate(-4, -4)};
        HalfPlaneHint lastBadHint = new HalfPlaneHint(new Coordinate(0, 0),
                new Coordinate(0.7377637010688854, -0.675059050294965));
        testRectHint(rect, lastBadHint, 0);

        HalfPlaneHint hint = new HalfPlaneHint(new Coordinate(0, 0),
                new Coordinate(0.6209474701786085, 0.7838521794820666));
        testRectHint(rect, hint, 3);
        rect = new Coordinate[]{new Coordinate(-2, 2), new Coordinate(2, 2),
                new Coordinate(2, -2), new Coordinate(-2, -2)};
        hint = new HalfPlaneHint(new Coordinate(0, 0),
                new Coordinate(0.7416025214414383, 0.6708395487683333));
        testRectHint(rect, hint, 4);

        rect = new Coordinate[]{
                new Coordinate(-2.159168821737699, 8.0),
                new Coordinate(8.0, 8.0),
                new Coordinate(8.0, -3.999532170942503),
                new Coordinate(-2.159168821737699, -3.999532170942503)
        };
        lastBadHint = new HalfPlaneHint(new Coordinate(2.9204156, 2.0002339),
                new Coordinate(3.5662858179937924, 2.7636811224775273));
        testRectHint(rect, lastBadHint, 3);
    }

    @Test
    public void testPhiRectangle() {
        Coordinate[] rect = new Coordinate[]{new Coordinate(-4, 4), new Coordinate(4, 4),
                new Coordinate(4, -4), new Coordinate(-4, -4)};
        Coordinate[] testRect = phiRectangle(3, rect);
        if (!doubleEqual(testRect[0].x, -4) || !doubleEqual(testRect[0].y, 4) ||
                !doubleEqual(testRect[1].x, 4) || !doubleEqual(testRect[1].y, 4) ||
                !doubleEqual(testRect[2].x, 4) || !doubleEqual(testRect[2].y, -4) ||
                !doubleEqual(testRect[3].x, -4) || !doubleEqual(testRect[3].y, -4)) {
            throw new AssertionError(Arrays.toString(testRect));
        }
    }

    public void testPhiHint() {
        Coordinate[] rect = new Coordinate[]{new Coordinate(-4, 4), new Coordinate(4, 4),
                new Coordinate(4, -4), new Coordinate(-4, -4)};
        HalfPlaneHint hint = new HalfPlaneHint(new Coordinate(0, 0),
                new Coordinate(0.6209474701786085, 0.7838521794820666));
        HalfPlaneHint testHint = phiHint(3, rect, hint);
        if (!doubleEqual(testHint.getRight().getX(), 0.7838521794820666) ||
                !doubleEqual(testHint.getRight().getY(), -0.6209474701786085)) {
            throw new AssertionError("right angle point is " + testHint.getRight() +
                    " and should equal (0.7838521794820666, -0.6209474701786085)");
        }
        if (!doubleEqual(testHint.getCenter().getX(), 0) ||
                !doubleEqual(testHint.getCenter().getY(), 0)) {
            throw new AssertionError("left angle point is " + testHint.getCenter() +
                    " and should equal (0.0, 0.0)");
        }
    }

    private void testOneRectanglePhiReverse(Coordinate[] toTransform) {
        for (int i = 0; i <= 7; i++) {
            Coordinate[] transformedRect = phiRectangle(i, toTransform);

            Coordinate[] transformationUndo = phiOtherRectangleInverse(i, toTransform, transformedRect);

            for (int j = 0; j < 4; j++) {
                if (
                        !doubleEqual(toTransform[j].x, transformationUndo[j].x) ||
                                !doubleEqual(toTransform[j].y, transformationUndo[j].y)
                ) {
                    throw new AssertionError(
                            "basicTrans = " + i + "\n" +
                                    "toTransform = \n" + Arrays.toString(toTransform) + "\n" +
                                    "transformationUndo = \n" + Arrays.toString(transformationUndo)
                    );
                }
            }
        }
    }

    @Test
    public void testPhiRectangleRectangleReverse() {
        testOneRectanglePhiReverse(
                new Coordinate[]{
                        new Coordinate(-64, 64),
                        new Coordinate(64, 64),
                        new Coordinate(64, -64),
                        new Coordinate(-64, -64),
                });
        testOneRectanglePhiReverse(
                new Coordinate[]{
                        new Coordinate(-30, 28),
                        new Coordinate(-10, 28),
                        new Coordinate(-10, 26),
                        new Coordinate(-30, 26)
                }
        );
    }

    private void testRectangleScan(Coordinate[] rectangleToTest, Coordinate[] stepsExpectedResult) {
        SearchPathPrototype result = rectangleScan(rectangleToTest[0], rectangleToTest[1], rectangleToTest[2],
                rectangleToTest[3], new SearchPathPrototype());

        if (result.getPoints().size() != stepsExpectedResult.length) {
            throw new AssertionError("The number of steps should be " + stepsExpectedResult.length + " " +
                    "but equals " + result.getPoints().size());
        }

        for (int i = 0; i < stepsExpectedResult.length; i++) {
            result.getPoints().get(i).getPrecisionModel().makePrecise(stepsExpectedResult[i]);
            if (!result.getPoints().get(i).getCoordinate().equals2D(
                    stepsExpectedResult[i])) {
                throw new AssertionError("The coordinate " + result.getPoints().get(i) + " does" +
                        " not equal the expected coordinate " + stepsExpectedResult[i]);
            }
        }
    }

    @Test
    public void rectangleScanTest() {
        Coordinate[] rectangleToTest = new Coordinate[]{
                new Coordinate(0, 1), new Coordinate(2, 1), new Coordinate(2, -100),
                new Coordinate(0, -100)
        };
        Coordinate[] stepsExpectedResult = new Coordinate[]{
                new Coordinate(0, 1), new Coordinate(0, -100), new Coordinate(1, -100),
                new Coordinate(1, 1), new Coordinate(2, 1), new Coordinate(2, -100)
        };
        testRectangleScan(rectangleToTest, stepsExpectedResult);

        rectangleToTest = new Coordinate[]{
                new Coordinate(2, 1), new Coordinate(2, -100), new Coordinate(0, -100),
                new Coordinate(0, 1)
        };
        testRectangleScan(rectangleToTest, stepsExpectedResult);
    }
}