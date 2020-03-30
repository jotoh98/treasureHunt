package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import java.util.Arrays;
import java.util.List;

import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper.*;
import static com.treasure.hunt.utils.JTSUtils.doubleEqual;

/**
 * @author Rank
 */

public class RoutinesFromPaperTest {

    @BeforeEach
    void setUp() {

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
    public void testBasicTransformation2() {
        Coordinate[] testRectangle = new Coordinate[]{
                new Coordinate(4748.7729876546655, 8192.0),
                new Coordinate(4752.857933732405, 8192.0),
                new Coordinate(4752.857933732405, 8187.334718411631),
                new Coordinate(4748.7729876546655, 8187.334718411464)
        };
        HalfPlaneHint testHint = new HalfPlaneHint(
                new Coordinate(4750.815460694, 8189.667359206001),
                new Coordinate(4751.637576194589, 8189.098038467145)
        );
        testRectHint(testRectangle, testHint, 0);
    }

    @Test
    public void testBasicTransformation3() { // hint equals the diagonal
        Coordinate[] testRectangle = new Coordinate[]{
                new Coordinate(6.388583936417138, 16.0), new Coordinate(16.0, 16.0),
                new Coordinate(16.0, 7.037209521563136), new Coordinate(6.388583936417138, 7.037209521563136)
        };
        HalfPlaneHint testHint = new HalfPlaneHint(new Coordinate(11.194292000000004, 11.518604800000002),
                new Coordinate(11.925645701619175, 10.836606439937503));
        testRectHint(testRectangle, testHint, 0);
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
        SearchPath result = rectangleScan(rectangleToTest[0], rectangleToTest[1], rectangleToTest[2],
                rectangleToTest[3], new SearchPath());

        if (result.getPoints().size() != stepsExpectedResult.length) {
            throw new AssertionError("The number of steps should be " + stepsExpectedResult.length + " " +
                    "but equals " + result.getPoints().size() + "\n result:\n" + result.getPoints()
                    + "\n expected result:\n" + Arrays.toString(stepsExpectedResult));
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

    private void assertArrayEqualsList(List<Point> actualResult, Point[] correctResult) {
        if (correctResult.length != actualResult.size()) {
            throw new AssertionError("The actual result has more steps than the correct result\nactual result:\n "
                    + actualResult + "\ncorrect result:\n " + Arrays.toString(correctResult));
        }
        for (int i = 0; i < correctResult.length; i++) {
            if (!correctResult[i].equalsExact(actualResult.get(i))) {
                throw new AssertionError("The actual result does not equal the correct result\nactual result:\n "
                        + actualResult + "\ncorrect result:\n " + Arrays.toString(correctResult));
            }
        }
    }

    @Test
    public void rectangleScanEnhancedTestOne() {
        Point a = JTSUtils.createPoint(1, 4);
        Point b = JTSUtils.createPoint(2.5, 3);
        Point c = JTSUtils.createPoint(-0.5, -1.5);
        Point d = JTSUtils.createPoint(-1.5, -0.5);
        Vector2D aToBDividedByTwo = new Vector2D(a.getCoordinate(), b.getCoordinate());
        aToBDividedByTwo = aToBDividedByTwo.divide(2);
        Point[] correctResult = new Point[]{
                JTSUtils.createPoint(a.getX() + aToBDividedByTwo.getX(),
                        a.getY() + aToBDividedByTwo.getY()),
                JTSUtils.createPoint(d.getX() + aToBDividedByTwo.getX(),
                        d.getY() + aToBDividedByTwo.getY())
        };
        List<Point> actualResult = RoutinesFromPaper.rectangleScanEnhanced(a.getCoordinate(), b.getCoordinate(),
                c.getCoordinate(), d.getCoordinate(), new SearchPath()).getPoints();
        assertArrayEqualsList(actualResult, correctResult);
    }

    @Test
    public void rectangleScanEnhancedTestTwo() {
        //todo
    }
}