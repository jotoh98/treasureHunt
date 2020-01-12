package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.Arrays;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.right;
import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.up;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils.assertRectangle;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils.centerOfRectangle;
import static com.treasure.hunt.utils.JTSUtils.lineWayIntersection;

import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils.*;

/**
 * @author bsen
 */
public class RoutinesFromPaper {
    static Movement rectangleScan(Point A, Point B, Point C, Point D, Movement move) {
        return rectangleScan(A.getCoordinate(), B.getCoordinate(), C.getCoordinate(), D.getCoordinate(), move);
    }

    static Movement rectangleScan(Coordinate A, Coordinate B, Coordinate C, Coordinate D, Movement move) {
        int k = (int) A.distance(B);
        Point[] a = new Point[k + 1];
        Point[] b = new Point[k + 1];

        { //create a_i on line segment AB
            double xDist = B.getX() - A.getX();
            double yDist = B.getY() - A.getY();
            for (int i = 0; i <= k; i++) {
                a[i] = JTSUtils.createPoint(A.getX() + xDist * ((double) i / (double) k), A.getY() + yDist *
                        ((double) i / (double) k));
            }
        }
        { //create b_i on line segment DC
            double xDist = C.getX() - D.getX();
            double yDist = C.getY() - D.getY();
            //for (int i = 0; i <= k; i++) {
            for (int i = 0; i <= k; i++) {
                b[i] = JTSUtils.createPoint(D.getX() + xDist * ((double) i / (double) k), D.getY() + yDist *
                        ((double) i / (double) k));
            }
        }

        if (k % 2 == 1) //code like in paper
        {
            //for (int i = 0; i <= k - 1; k += 2) {
            for (int i = 0; i <= k - 1; i += 2) {
                move.addWayPoint(a[i]);
                move.addWayPoint(b[i]);
                move.addWayPoint(b[i + 1]);
                move.addWayPoint(a[i + 1]);
            }
        } else {
            //for (int i = 0; i <= k - 2; k += 2) {
            for (int i = 0; i <= k - 2; i += 2) {
                move.addWayPoint(a[i]);
                move.addWayPoint(b[i]);
                move.addWayPoint(b[i + 1]);
                move.addWayPoint(a[i + 1]);
            }
            move.addWayPoint(a[k]);
            move.addWayPoint(b[k]);
        }
        //move.addWayPoint(a); // go to a
        return move;
    }
    /**
     * Returns the result of rho, defined by rectangle rect, applied on hint.
     *
     * @param rect defines rho
     * @param hint used as input in rho
     * @return
     */
    static HalfPlaneHint rhoHint(Coordinate[] rect, HalfPlaneHint hint) {
        assertRectangle(rect);
        LineSegment AB = new LineSegment(rect[0], rect[1]);
        LineSegment CD = new LineSegment(rect[2], rect[3]);
        Coordinate centerAB = AB.midPoint();
        Coordinate centerCD = CD.midPoint();
        AffineTransformation reflection = AffineTransformation.reflectionInstance(centerAB.getX(), centerAB.getY(),
                centerCD.getX(), centerCD.getY());
        Coordinate newAPLeft = new Coordinate();
        Coordinate newAPRight = new Coordinate();
        reflection.transform(hint.getRightPoint(), newAPLeft);
        reflection.transform(hint.getLeftPoint(), newAPRight);
        HalfPlaneHint newHint = new HalfPlaneHint(newAPLeft, newAPRight);
        return newHint;
    }

    /**
     * Returns the result of rho, defined by rectangle rect, applied on P.
     *
     * @param rect
     * @param P
     * @return
     */
    static Coordinate rhoPoint(Coordinate[] rect, Coordinate P) {
        assertRectangle(rect);
        LineSegment AB = new LineSegment(rect[0], rect[1]);
        LineSegment CD = new LineSegment(rect[2], rect[3]);
        Coordinate centerAB = AB.midPoint();
        Coordinate centerCD = CD.midPoint();
        AffineTransformation reflection = AffineTransformation.reflectionInstance(centerAB.getX(), centerAB.getY(),
                centerCD.getX(), centerCD.getY());

        Coordinate transformedP = new Coordinate();
        return reflection.transform(P, transformedP);
    }

    /**
     * sigma is defined like in the paper, P is the Point which is to be transformed and r is the middle point of the
     * rectangle, i is the index.
     *
     * @param i the index of sigma
     * @param r the center of a rectangle which is used as the point to rotate around
     * @param P the Point which is to be transformed
     * @return
     */
    static Coordinate sigmaPoint(int i, Coordinate r, Coordinate P) {
        //AffineTransformation rot_i = AffineTransformation.rotationInstance(Math.PI * i / 2, r.getX(), r.getY());
        AffineTransformation rot_i;
        switch (i) {
            case 0:
                rot_i = new AffineTransformation(new double[]{1, 0, 0, 0, 1, 0});
                break;
            case 1:
                rot_i = new AffineTransformation(new double[]{0, -1, 0, 1, 0, 0});
                break;
            case 2:
                rot_i = new AffineTransformation(new double[]{-1, 0, 0, 0, -1, 0});
                break;
            case 3:
                rot_i = new AffineTransformation(new double[]{0, 1, 0, -1, 0, 0});
                break;
            default:
                throw new IllegalArgumentException("i should be in [0,3] but equals " + i);
        }
        Coordinate ret = new Coordinate(P.x - r.x, P.y - r.y);
        rot_i.transform(ret, ret);
        ret.x = ret.x + r.x;
        ret.y = ret.y + r.y;
        return ret;

    }

    static Coordinate sigmaPointReverse(int i, Coordinate r, Coordinate P) {
        return sigmaPoint(3 - i, r, P);
    }

    /**
     * Returns the result of sigma, defined by index i and rectangle rect, applied on the points in rect.
     *
     * @param i
     * @param rect
     * @return
     */
    static Coordinate[] sigmaRectangle(int i, Coordinate[] rect) {
        assertRectangle(rect);
        Coordinate r = centerOfRectangle(rect);

        AffineTransformation rotHalfPi = new AffineTransformation(new double[]{0, -1, 0, 1, 0, 0});

        if (i == 0 || i == 2) {
            return rect;
        }
        if (i == 1 || i == 3) {
            //rotate rectangle by pi/2
            Coordinate[] transformed = new Coordinate[]{new Coordinate(), new Coordinate(),
                    new Coordinate(), new Coordinate()};
            for (int j = 0; j <= 3; j++) {
                transformed[j].x = rect[j].x - r.x;
                transformed[j].y = rect[j].y - r.y;
            }

            Coordinate transformed0old = transformed[0].copy();
            rotHalfPi.transform(transformed[1], transformed[0]);
            rotHalfPi.transform(transformed[2], transformed[1]);
            rotHalfPi.transform(transformed[3], transformed[2]);
            rotHalfPi.transform(transformed0old, transformed[3]);

            for (int j = 0; j <= 3; j++) {
                transformed[j].x = transformed[j].x + r.x;
                transformed[j].y = transformed[j].y + r.y;
            }
            return transformed;
        }
        throw new IllegalArgumentException("i should be in [0,3] but is equal to " + i);
    }

    static Coordinate[] sigmaRectangleReverse(int i, Coordinate[] rect) {
        return sigmaRectangle(i, rect);
    }

    /**
     * Returns the result of phi, defined by rect, with index i, applied on rect.
     *
     * @param i
     * @param rect
     * @return
     */
    static Coordinate[] phiRectangle(int i, Coordinate[] rect) {
        assertRectangle(rect);
        if (i < 0 || i > 7)
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        return sigmaRectangle(i % 4, rect);
    }


    static Coordinate phiPoint(int i, Coordinate[] rect, Coordinate P) {
        assertRectangle(rect);
        if (i < 0 || i > 7)
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        Coordinate r = centerOfRectangle(rect);
        if (i < 4)
            return sigmaPoint(i, r, P);
        return sigmaPoint(i, r, rhoPoint(rect, P));
    }

    /**
     * Returns the result of phi, defined by rect, with index i, applied on hint.
     *
     * @param i    the index of phi
     * @param rect the rectangle which defines phi
     * @param hint the hint that is used as input in phi
     * @return
     */
    static HalfPlaneHint phiHint(int i, Coordinate[] rect, HalfPlaneHint hint) {
        assertRectangle(rect);
        if (i < 0 || i > 7)
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        Coordinate r = centerOfRectangle(rect);
        HalfPlaneHint transformedHint = new HalfPlaneHint(
                sigmaPoint(i % 4, r, hint.getLeftPoint()),
                sigmaPoint(i % 4, r, hint.getRightPoint())
        );
        if (i < 4)
            return transformedHint;
        return rhoHint(rect, transformedHint);
    }

    /**
     * Calculates the inverse phi operation defined by the rectangle rect and applies it on P.
     *
     * @param i    the index of phi
     * @param rect the rectangle which defines phi (and therefore also the inverse of phi)
     * @param P
     * @return
     */
    static Coordinate phiPointInverse(int i, Coordinate[] rect, Coordinate P) {
        assertRectangle(rect);
        if (i < 0 || i > 7)
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        Coordinate r = centerOfRectangle(rect);
        if (i < 4) {
            return sigmaPointReverse(i, r, P);
        }
        return sigmaPointReverse(i - 4, r, rhoPoint(rect, P));
    }

    static Movement rectangleScanPhiReverse(int basicTrans, Coordinate[] phiRect,
                                             Coordinate A, Coordinate B, Coordinate C, Coordinate D, Movement move) {
        return rectangleScan(
                phiPointInverse(basicTrans, phiRect, A),
                phiPointInverse(basicTrans, phiRect, B),
                phiPointInverse(basicTrans, phiRect, C),
                phiPointInverse(basicTrans, phiRect, D),
                move
        );
    }

    /**
     * Returns the basic transformation for a rectangle-hint-pair, of which the definition can be found in the paper.
     * (Page 8, below Proposition 3.2)
     *
     * @param rect
     * @param hint
     * @return
     */
    static int getBasicTransformation(Coordinate[] rect, HalfPlaneHint hint) {
        for (int i = 0; i <= 7; i++) {
            Coordinate[] testRect = phiRectangle(i, rect);
            HalfPlaneHint testHint = phiHint(i, rect, hint);
            LineSegment hintLine = new LineSegment(testHint.getLeftPoint(),
                    testHint.getRightPoint());
            LineSegment testAD = new LineSegment(testRect[0], testRect[3]);
            Coordinate AD_hint = lineWayIntersection(hintLine, testAD);
            if (testHint.getDirection() == up)
                return i;
            if (testHint.getDirection() == right &&
                    testHint.getUpperHintPoint().getX() < testHint.getLowerHintPoint().getX() &&
                    AD_hint != null)
                return i;
        }
        System.out.println("rect: " + Arrays.toString(rect) + " anglepoints: "
                + hint.getRightPoint() + hint.getLeftPoint());
        throw new IllegalArgumentException("Somehow there was no basic transformation found for this " +
                "rectangle and hint. This is impossible.");
    }

    /**
     * Inverts the by rect defined phi , but calculates and returns the inverse points of the points in toTransform.
     *
     * @param i           the index of phi
     * @param rect        the rectangle which defines phi (and therefore also the inverse of phi)
     * @param toTransform
     * @return
     */
    static Coordinate[] phiOtherRectangleInverse(int i, Coordinate[] rect, Coordinate[] toTransform) {
        //TODO: build arrangeRectangle in this method
        assertRectangle(rect);
        assertRectangle(toTransform);
        Coordinate[] ret = new Coordinate[]{
                phiPointInverse(i, rect, toTransform[0]),
                phiPointInverse(i, rect, toTransform[1]),
                phiPointInverse(i, rect, toTransform[2]),
                phiPointInverse(i, rect, toTransform[3])
        };
        return arrangeRectangle(ret);
    }

}
