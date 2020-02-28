package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.utils.JTSUtils.GEOMETRY_FACTORY;
import static com.treasure.hunt.utils.JTSUtils.lineWayIntersection;

/**
 * Methods used to reduce the rectangle in the strategy of the paper, when the hint was good.
 * //TODO explain whats a good hint
 * // checks if the hint is good (i.e. if the hint divides one side of the rectangles in into two parts such that
 * // the smaller one is bigger or equal to 1)
 *
 * @author bsen
 */
public class ReduceRectangle {
    /**
     * If the hint-line goes through the lines (searchAreaCornerA, searchAreaCornerB) and
     * (searchAreaCornerC, searchAreaCornerD) and the hint is good (defined in the paper and explained above),
     * the corners of the biggest axis parallel-rectangle which lies in the
     * rectangle ABCD (determined by its corners searchAreaCornerA, searchAreaCornerB, etc.) and where the
     * treasure could be located, due to the information gained by the hint, is returned.
     * Otherwise the return value is null.
     *
     * @param searchAreaCornerA
     * @param searchAreaCornerB
     * @param searchAreaCornerC
     * @param searchAreaCornerD
     * @param hint
     * @param hintLine
     * @return the corners of the reduced rectangle if this kind of split is valid, null otherwise
     */
    static Point[] splitRectangleHorizontally(Point searchAreaCornerA, Point searchAreaCornerB, Point searchAreaCornerC,
                                              Point searchAreaCornerD, HalfPlaneHint hint, LineSegment hintLine) {
        LineSegment BC = new LineSegment(searchAreaCornerB.getCoordinate(), searchAreaCornerC.getCoordinate());
        LineSegment AD = new LineSegment(searchAreaCornerA.getCoordinate(), searchAreaCornerD.getCoordinate());

        Coordinate intersectionADHint = JTSUtils.lineWayIntersection(hintLine, AD);
        Coordinate intersectionBCHint = JTSUtils.lineWayIntersection(hintLine, BC);

        // checks if the hint is good (i.e. if the hint divides one side of the rectangles in into two parts such that
        // the smaller one is bigger or equal to 1)
        if (intersectionADHint == null || intersectionBCHint == null ||
                intersectionADHint.distance(searchAreaCornerA.getCoordinate()) < 1 ||
                intersectionADHint.distance(searchAreaCornerD.getCoordinate()) < 1 ||
                intersectionBCHint.distance(searchAreaCornerB.getCoordinate()) < 1 ||
                intersectionBCHint.distance(searchAreaCornerC.getCoordinate()) < 1
        ) {
            return null;
        }

        if (hint.getDirection() == up) {
            Coordinate newC = intersectionBCHint;
            Coordinate newD = intersectionADHint;
            return new Point[]{searchAreaCornerA, searchAreaCornerB, GEOMETRY_FACTORY.createPoint(newC),
                    GEOMETRY_FACTORY.createPoint(newD)};
        }
        if (hint.getDirection() == down) {
            Coordinate newA = intersectionADHint;
            Coordinate newB = intersectionBCHint;
            return new Point[]{GEOMETRY_FACTORY.createPoint(newA), GEOMETRY_FACTORY.createPoint(newB),
                    searchAreaCornerC, searchAreaCornerD};
        }

        if (hint.pointsUpwards()) {
            if (intersectionADHint.distance(searchAreaCornerD.getCoordinate())
                    >= intersectionBCHint.distance(searchAreaCornerC.getCoordinate())) {
                Coordinate newD = new Coordinate(searchAreaCornerD.getX(), intersectionBCHint.getY());
                Coordinate newC = intersectionBCHint;
                return new Point[]{searchAreaCornerA, searchAreaCornerB, GEOMETRY_FACTORY.createPoint(newC),
                        GEOMETRY_FACTORY.createPoint(newD)};
            } else {
                Coordinate newC = new Coordinate(searchAreaCornerC.getX(), intersectionADHint.getY());
                Coordinate newD = intersectionADHint;
                return new Point[]{searchAreaCornerA, searchAreaCornerB, GEOMETRY_FACTORY.createPoint(newC),
                        GEOMETRY_FACTORY.createPoint(newD)};
            }
        }
        if (hint.pointsDownwards()) {
            if (intersectionADHint.distance(searchAreaCornerA.getCoordinate()) >= intersectionBCHint.distance(
                    searchAreaCornerB.getCoordinate())) {
                Coordinate newA = new Coordinate(searchAreaCornerA.getX(), intersectionBCHint.getY());
                Coordinate newB = intersectionBCHint;
                return new Point[]{GEOMETRY_FACTORY.createPoint(newA), GEOMETRY_FACTORY.createPoint(newB),
                        searchAreaCornerC, searchAreaCornerD};
            } else {
                Coordinate newB = new Coordinate(searchAreaCornerB.getX(), intersectionADHint.getY());
                Coordinate newA = intersectionADHint;
                return new Point[]{GEOMETRY_FACTORY.createPoint(newA), GEOMETRY_FACTORY.createPoint(newB),
                        searchAreaCornerC, searchAreaCornerD};
            }
        }
        return null;
    }

    /**
     * If the hint-line goes through the lines (searchAreaCornerA, searchAreaCornerB) and
     * (searchAreaCornerC, searchAreaCornerD) and the hint is good (defined in the paper and explained above),
     * the biggest axis-parallel rectangle which lies in the rectangle ABCD (determined by its corners searchAreaCornerA,
     * searchAreaCornerB, etc.) and where the treasure could be located, due to the information gained by the hint,
     * is returned.
     * Otherwise the return value is null.
     *
     * @param searchAreaCornerA
     * @param searchAreaCornerB
     * @param searchAreaCornerC
     * @param searchAreaCornerD
     * @param hint
     * @param hintLine
     * @return the corners of the reduced rectangle if this kind of split is valid, null otherwise
     */
    static Point[] splitRectangleVertically(Point searchAreaCornerA, Point searchAreaCornerB, Point searchAreaCornerC,
                                            Point searchAreaCornerD, HalfPlaneHint hint,
                                            LineSegment hintLine) {
        LineSegment AB = new LineSegment(searchAreaCornerA.getCoordinate(), searchAreaCornerB.getCoordinate());
        LineSegment CD = new LineSegment(searchAreaCornerC.getCoordinate(), searchAreaCornerD.getCoordinate());

        Coordinate intersectionABHint = lineWayIntersection(hintLine, AB);
        Coordinate intersectionCDHint = lineWayIntersection(hintLine, CD);

        // checks if the hint is good (i.e. if the hint divides one side of the rectangles in into two parts such that
        // the smaller one is bigger or equal to 1)
        if (intersectionABHint == null || intersectionCDHint == null
                || (intersectionABHint.distance(searchAreaCornerA.getCoordinate()) < 1
                || intersectionABHint.distance(searchAreaCornerB.getCoordinate()) < 1
                || intersectionCDHint.distance(searchAreaCornerC.getCoordinate()) < 1
                || intersectionCDHint.distance(searchAreaCornerD.getCoordinate()) < 1)) {
            return null;
        }

        if (hint.getDirection() == left) {
            // determine which intersection-point has to be used to calculate the rectangle-points:
            if (intersectionABHint.distance(searchAreaCornerB.getCoordinate()) >= intersectionCDHint.distance(
                    searchAreaCornerC.getCoordinate())) {
                Coordinate newB = new Coordinate(intersectionCDHint.getX(), searchAreaCornerB.getY());
                Coordinate newC = intersectionCDHint;
                return new Point[]{searchAreaCornerA, GEOMETRY_FACTORY.createPoint(newB),
                        GEOMETRY_FACTORY.createPoint(newC), searchAreaCornerD};
            } else {
                Coordinate newC = new Coordinate(intersectionABHint.getX(), searchAreaCornerC.getY());
                Coordinate newB = intersectionABHint;
                return new Point[]{searchAreaCornerA, GEOMETRY_FACTORY.createPoint(newB),
                        GEOMETRY_FACTORY.createPoint(newC), searchAreaCornerD};
            }
        }

        if (hint.getDirection() == right) {
            // determine which intersection-point has to be used to calculate the rectangle-points:
            if (intersectionABHint.distance(searchAreaCornerA.getCoordinate()) >=
                    intersectionCDHint.distance(searchAreaCornerD.getCoordinate())) {
                Coordinate newA = new Coordinate(intersectionCDHint.getX(), searchAreaCornerA.getY());
                Coordinate newD = intersectionCDHint;
                return new Point[]{GEOMETRY_FACTORY.createPoint(newA), searchAreaCornerB, searchAreaCornerC,
                        GEOMETRY_FACTORY.createPoint(newD)};
            } else {
                Coordinate newD = new Coordinate(intersectionABHint.getX(), searchAreaCornerD.getY());
                Coordinate newA = intersectionABHint;
                return new Point[]{GEOMETRY_FACTORY.createPoint(newA), searchAreaCornerB, searchAreaCornerC,
                        GEOMETRY_FACTORY.createPoint(newD)};
            }
        }
        return null;
    }

    /**
     * When the rotation of a instance of StrategyFromPaper is not 0 this method is used to reduce the current search
     * rectangle when the hint is good.
     * If the hint is bad (defined in the paper and explained above), null is returned otherwise there are two cases:
     * <p>
     * If the horizontal-parameter is true, this method has to be called as following:
     * aOne = searchAreaCornerA
     * aTwo = searchAreaCornerD
     * bOne = searchAreaCornerB
     * bTwo = searchAreaCornerC
     * and then it returns the reduced rectangle if the hint goes through the lines
     * (searchAreaCornerA, searchAreaCornerD) and
     * (searchAreaCornerB, searchAreaCornerC)
     * if it does not go through this lines (and the horizontal-parameter equals true), the method returns null
     * <p>
     * If the horizontal-parameter is false, this method has to be called as following:
     * aOne = searchAreaCornerA
     * aTwo = searchAreaCornerB
     * bOne = searchAreaCornerD
     * bTwo = searchAreaCornerC
     * and then it returns the reduced rectangle if the hint goes through the lines
     * (searchAreaCornerA, searchAreaCornerB) and
     * (searchAreaCornerC, searchAreaCornerD)
     * if it does not go through this lines (and the horizontal-parameter equals false), the method returns null.
     *
     * @param aOne
     * @param aTwo
     * @param bOne
     * @param bTwo
     * @param hint
     * @param horizontal
     * @return the corners of the reduced rectangle if this kind of split is valid, null otherwise
     */
    static Point[] splitWithRotation(Point aOne, Point aTwo, Point bOne, Point bTwo, HalfPlaneHint hint,
                                     boolean horizontal) {
        LineSegment hintLine = hint.getHalfPlaneLine();
        LineSegment aLine = new LineSegment(aOne.getCoordinate(), aTwo.getCoordinate());
        LineSegment bLine = new LineSegment(bOne.getCoordinate(), bTwo.getCoordinate());
        Coordinate intersectionAHint = JTSUtils.lineWayIntersection(hintLine, aLine);
        Coordinate intersectionBHint = JTSUtils.lineWayIntersection(hintLine, bLine);

        if (intersectionAHint == null || intersectionBHint == null ||
                intersectionAHint.distance(aOne.getCoordinate()) < 1 ||
                intersectionAHint.distance(aTwo.getCoordinate()) < 1 ||
                intersectionBHint.distance(bOne.getCoordinate()) < 1 ||
                intersectionBHint.distance(bTwo.getCoordinate()) < 1
        ) {
            return null;
        }

        boolean aOneInHint = hint.inHalfPlane(aOne.getCoordinate());
        boolean aTwoInHint = hint.inHalfPlane(aTwo.getCoordinate());
        boolean bOneInHint = hint.inHalfPlane(bOne.getCoordinate());
        boolean bTwoInHint = hint.inHalfPlane(bTwo.getCoordinate());
        if ((aOneInHint && (aTwoInHint || bTwoInHint)) || (bOneInHint && (aTwoInHint || bTwoInHint)))
            throw new RuntimeException("Somehow two opposing points are in the hint");
        if (aOneInHint || bOneInHint) {
            double distanceAOneHintIntersection = aOne.getCoordinate().distance(intersectionAHint);
            double distanceBOneHintIntersection = bOne.getCoordinate().distance(intersectionBHint);
            if (distanceAOneHintIntersection < distanceBOneHintIntersection) {
                Vector2D bOneToAOne = new Vector2D(bOne.getCoordinate(), aOne.getCoordinate());
                if (horizontal) {
                    return new Point[]{
                            aOne, bOne,
                            GEOMETRY_FACTORY.createPoint(intersectionBHint),
                            JTSUtils.createPoint(intersectionBHint.x + bOneToAOne.getX(),
                                    intersectionBHint.y + bOneToAOne.getY())
                    };
                } else {
                    return new Point[]{
                            aOne,
                            JTSUtils.createPoint(intersectionBHint.x + bOneToAOne.getX(),
                                    intersectionBHint.y + bOneToAOne.getY()),
                            GEOMETRY_FACTORY.createPoint(intersectionBHint),
                            bOne
                    };
                }
            } else {
                Vector2D aOneToBOne = new Vector2D(aOne.getCoordinate(), bOne.getCoordinate());
                if (horizontal) {
                    return new Point[]{
                            aOne, bOne,
                            JTSUtils.createPoint(intersectionAHint.x + aOneToBOne.getX(),
                                    intersectionAHint.y + aOneToBOne.getY()),
                            GEOMETRY_FACTORY.createPoint(intersectionAHint)
                    };
                } else {
                    return new Point[]{
                            aOne,
                            GEOMETRY_FACTORY.createPoint(intersectionAHint),
                            JTSUtils.createPoint(intersectionAHint.x + aOneToBOne.getX(),
                                    intersectionAHint.y + aOneToBOne.getY()),
                            bOne
                    };
                }
            }
        }
        if (aTwoInHint || bTwoInHint) {
            double distanceATwoHintIntersection = aTwo.getCoordinate().distance(intersectionAHint);
            double distanceBTwoHintIntersection = bTwo.getCoordinate().distance(intersectionBHint);
            if (distanceATwoHintIntersection < distanceBTwoHintIntersection) {
                Vector2D bTwoToATwo = new Vector2D(bTwo.getCoordinate(), aTwo.getCoordinate());
                if (horizontal) {
                    return new Point[]{
                            JTSUtils.createPoint(intersectionBHint.x + bTwoToATwo.getX(),
                                    intersectionBHint.y + bTwoToATwo.getY()),
                            GEOMETRY_FACTORY.createPoint(intersectionBHint),
                            bTwo,
                            aTwo
                    };
                } else {
                    return new Point[]{
                            JTSUtils.createPoint(intersectionBHint.x + bTwoToATwo.getX(),
                                    intersectionBHint.y + bTwoToATwo.getY()),
                            aTwo,
                            bTwo,
                            GEOMETRY_FACTORY.createPoint(intersectionBHint)
                    };
                }
            } else {
                Vector2D aTwoToBTwo = new Vector2D(aTwo.getCoordinate(), bTwo.getCoordinate());
                if (horizontal) {
                    return new Point[]{
                            GEOMETRY_FACTORY.createPoint(intersectionAHint),
                            JTSUtils.createPoint(intersectionAHint.x + aTwoToBTwo.getX(),
                                    intersectionAHint.y + aTwoToBTwo.getY()),
                            bTwo,
                            aTwo
                    };
                } else {
                    return new Point[]{
                            GEOMETRY_FACTORY.createPoint(intersectionAHint),
                            aTwo,
                            bTwo,
                            JTSUtils.createPoint(intersectionAHint.x + aTwoToBTwo.getX(),
                                    intersectionAHint.y + aTwoToBTwo.getY())
                    };
                }
            }
        }
        throw new RuntimeException("The hint-line does not touch the current rectangle.");
    }
}
