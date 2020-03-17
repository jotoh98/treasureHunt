package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.Arrays;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.right;
import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.up;
import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.GeometricUtils.*;
import static com.treasure.hunt.utils.JTSUtils.lineWayIntersection;

/**
 * For an explanation what each method does, it is recommended to look in the paper (Deterministic Treasure Hunt in the
 * Plane with Angular Hints from Bouchard et al.).
 * There the same symbols are used.
 * The functionality of rectangleScan is explained there on page 3.
 * Phi, rho, sigma and the basic-transformation are explained on pages 7ff.
 *
 * @author Rank
 */
public class RoutinesFromPaper {
    private RoutinesFromPaper() {
    }

    /**
     * @param k the distance from P1 to P2
     * @return An Array of Points on P1P2, which have distance one to one another and reach from P1 to P2 at minimum
     */
    static private Point[] lineOfPointsWithDistanceOne(int k, Coordinate P1, Coordinate P2) {
        Point[] res = new Point[k + 1];

        double xDist = P2.getX() - P1.getX();
        double yDist = P2.getY() - P1.getY();
        for (int i = 0; i <= k; i++) {
            res[i] = JTSUtils.createPoint(P1.getX() + xDist * ((double) i / (double) k), P1.getY() + yDist *
                    ((double) i / (double) k));
        }
        return res;
    }

    /**
     * Does the same as the routine RectangleScan in the paper.
     * It adds the Points to searchPath so that the player sees all points in the rectangle ABCD.
     * Unlike the paper it does not add the point where the procedure started to the search-path.
     */
    public static SearchPath rectangleScan(Point A, Point B, Point C, Point D, SearchPath searchPath) {
        return rectangleScan(A.getCoordinate(), B.getCoordinate(), C.getCoordinate(), D.getCoordinate(), searchPath);
    }

    /**
     * @see RoutinesFromPaper#rectangleScan(Point, Point, Point, Point, SearchPath)
     */
    public static SearchPath rectangleScan(Coordinate A, Coordinate B, Coordinate C, Coordinate D, SearchPath searchPath) {
        if (A.distance(B) > A.distance(D)) {
            Coordinate temp = A;
            A = D;
            D = C;
            C = B;
            B = temp;
        }

        int k = (int) A.distance(B);

        if (k == 0) {
            searchPath.addPoint(JTSUtils.GEOMETRY_FACTORY.createPoint(A));
            searchPath.addPoint(JTSUtils.GEOMETRY_FACTORY.createPoint(C));
            return searchPath;
        }

        Point[] a = lineOfPointsWithDistanceOne(k, A, B);
        Point[] b = lineOfPointsWithDistanceOne(k, D, C);

        if (k % 2 == 1) {
            for (int i = 0; i <= k - 1; i += 2) {
                searchPath.addPoint(a[i]);
                searchPath.addPoint(b[i]);
                searchPath.addPoint(b[i + 1]);
                searchPath.addPoint(a[i + 1]);
            }
        } else {
            for (int i = 0; i <= k - 2; i += 2) {
                searchPath.addPoint(a[i]);
                searchPath.addPoint(b[i]);
                searchPath.addPoint(b[i + 1]);
                searchPath.addPoint(a[i + 1]);
            }
            searchPath.addPoint(a[k]);
            searchPath.addPoint(b[k]);
        }
        return searchPath;
    }

    /**
     * Returns the result of rho, defined rectangle, applied on hint.
     *
     * @param rectangle the rectangle defining rho
     * @param hint used as input in rho
     * @return the result of rho(hint)
     */
    static HalfPlaneHint rhoHint(Coordinate[] rectangle, HalfPlaneHint hint) {
        assertRectangle(rectangle);
        LineSegment AB = new LineSegment(rectangle[0], rectangle[1]);
        LineSegment CD = new LineSegment(rectangle[2], rectangle[3]);
        Coordinate centerAB = AB.midPoint();
        Coordinate centerCD = CD.midPoint();
        AffineTransformation reflection = AffineTransformation.reflectionInstance(centerAB.getX(), centerAB.getY(),
                centerCD.getX(), centerCD.getY());
        Coordinate newAPLeft = new Coordinate();
        Coordinate newAPRight = new Coordinate();
        reflection.transform(hint.getRight(), newAPLeft);
        reflection.transform(hint.getCenter(), newAPRight);
        HalfPlaneHint newHint = new HalfPlaneHint(newAPLeft, newAPRight);
        return newHint;
    }

    /**
     * Returns the result of rho, defined by rectangle, applied on P.
     *
     * @param rectangle the rectangle defining rho
     * @param P    used as input in rho
     * @return the result of rho(P)
     */
    static Coordinate rhoPoint(Coordinate[] rectangle, Coordinate P) {
        assertRectangle(rectangle);
        LineSegment AB = new LineSegment(rectangle[0], rectangle[1]);
        LineSegment CD = new LineSegment(rectangle[2], rectangle[3]);
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
     * @param P the Point which is used as input in sigma
     * @return sigma_i(P), with sigma being defined by r
     */
    static Coordinate sigmaPoint(int i, Coordinate r, Coordinate P) {
        AffineTransformation rotationI; //rot_i
        switch (i) {
            case 0:
                rotationI = new AffineTransformation(new double[]{1, 0, 0, 0, 1, 0});
                break;
            case 1:
                rotationI = new AffineTransformation(new double[]{0, -1, 0, 1, 0, 0});
                break;
            case 2:
                rotationI = new AffineTransformation(new double[]{-1, 0, 0, 0, -1, 0});
                break;
            case 3:
                rotationI = new AffineTransformation(new double[]{0, 1, 0, -1, 0, 0});
                break;
            default:
                throw new IllegalArgumentException("i should be in [0,3] but equals " + i);
        }
        Coordinate ret = new Coordinate(P.x - r.x, P.y - r.y);
        rotationI.transform(ret, ret);
        ret.x = ret.x + r.x;
        ret.y = ret.y + r.y;
        return ret;

    }

    /**
     * Reverses {@link RoutinesFromPaper#sigmaPoint(int, Coordinate, Coordinate)}
     */
    static Coordinate sigmaPointReverse(int i, Coordinate r, Coordinate P) {
        if (i < 0 || i > 3) {
            throw new IllegalArgumentException("i should be in [0,3] but is equal to " + i);
        }
        return sigmaPoint((4 - i) % 4, r, P);
    }

    /**
     * Returns the result of sigma_i defined by rectangle, applied on the points in rectangle.
     *
     * @param i    the index of sigma
     * @param rectangle the rectangle sigma is defined by and applied on
     * @return sigma_i(rectangle) with sigma being defined by rectangle
     */
    static Coordinate[] sigmaRectangle(int i, Coordinate[] rectangle) {
        assertRectangle(rectangle);

        if (i == 0 || i == 2) {
            return rectangle;
        }

        Coordinate r = centerOfRectangle(rectangle);
        AffineTransformation rotHalfPi = new AffineTransformation(new double[]{0, -1, 0, 1, 0, 0});
        if (i == 1 || i == 3) {
            //rotate rectangle by pi/2
            Coordinate[] transformed = new Coordinate[]{new Coordinate(), new Coordinate(),
                    new Coordinate(), new Coordinate()};
            for (int j = 0; j <= 3; j++) {
                transformed[j].x = rectangle[j].x - r.x;
                transformed[j].y = rectangle[j].y - r.y;
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

    /**
     * Returns the result of phi, defined by rectangle, with index i, applied on rectangle.
     *
     * @param i    the index of phi
     * @param rectangle the rectangle phi is defined by and applied on
     * @return phi_i(rectangle), with phi being defined by rectangle
     */
    static Coordinate[] phiRectangle(int i, Coordinate[] rectangle) {
        assertRectangle(rectangle);
        if (i < 0 || i > 7) {
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        }
        return sigmaRectangle(i % 4, rectangle);
    }

    /**
     * @param i    the index of phi
     * @param rectangle the rectangle defining phi
     * @param P    the point phi is applied on
     * @return phi_i(P), with phi being defined by rectangle
     */
    static Coordinate phiPoint(int i, Coordinate[] rectangle, Coordinate P) {
        assertRectangle(rectangle);
        if (i < 0 || i > 7) {
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        }
        Coordinate r = centerOfRectangle(rectangle);
        if (i < 4) {
            return sigmaPoint(i, r, P);
        }
        return rhoPoint(rectangle, sigmaPoint(i, r, P));
    }

    /**
     * Returns the result of phi, defined by rectangle, with index i, applied on hint.
     *
     * @param i    the index of phi
     * @param rectangle the rectangle defining phi
     * @param hint the hint that is used as input in phi
     * @return phi_i(hint), with phi being defined by rectangle
     */
    static HalfPlaneHint phiHint(int i, Coordinate[] rectangle, HalfPlaneHint hint) {
        assertRectangle(rectangle);
        if (i < 0 || i > 7) {
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        }
        Coordinate r = centerOfRectangle(rectangle);
        HalfPlaneHint transformedHint = new HalfPlaneHint(
                sigmaPoint(i % 4, r, hint.getCenter()),
                sigmaPoint(i % 4, r, hint.getRight())
        );
        if (i < 4) {
            return transformedHint;
        }
        return rhoHint(rectangle, transformedHint);
    }

    /**
     * Calculates the inverse of the phi operation defined by rectangle and applies it on P.
     *
     * @param i    the index of phi
     * @param rectangle the rectangle defining phi (and therefore also the inverse of phi)
     * @param P    the point the inverse of phi gets as input
     * @return the result of the inverse of phi_i applied on P, with phi being defined by rectangle
     */
    static Coordinate phiPointInverse(int i, Coordinate[] rectangle, Coordinate P) {
        assertRectangle(rectangle);
        if (i < 0 || i > 7) {
            throw new IllegalArgumentException("i must be in [0,7] but is " + i);
        }

        Coordinate r = centerOfRectangle(rectangle);
        if (i < 4) {
            return sigmaPointReverse(i, r, P);
        }
        return sigmaPointReverse(i - 4, r, rhoPoint(rectangle, P));
    }

    /**
     * Calculate phi_basicTrans, with phi being defined by the rectangle phiRect
     * Then adds the steps of the rectangleScan of phi_basicTrans(ABCD) to the searchPath.
     *
     * @param strategy   the strategy whose specificRectangleScan should be used
     * @return the input searchPath, rectangleScan applied to it
     */
    static SearchPath rectangleScanPhiReverse(int basicTrans, Coordinate[] phiRect, Coordinate A, Coordinate B,
                                              Coordinate C, Coordinate D, SearchPath searchPath, StrategyFromPaper strategy) {
        return strategy.specificRectangleScan(
                phiPointInverse(basicTrans, phiRect, A),
                phiPointInverse(basicTrans, phiRect, B),
                phiPointInverse(basicTrans, phiRect, C),
                phiPointInverse(basicTrans, phiRect, D),
                searchPath
        );
    }

    /**
     * Returns the basic transformation for a rectangle-hint-pair, of which the definition can be found in the paper.
     * (Page 8, below Proposition 3.2)
     *
     * @return the basic transformation of this rectangle-hint pair
     */
    static int getBasicTransformation(Coordinate[] rectangle, HalfPlaneHint hint) {
        for (int i = 0; i <= 7; i++) {
            Coordinate[] testRect = phiRectangle(i, rectangle);
            HalfPlaneHint testHint = phiHint(i, rectangle, hint);
            LineSegment hintLine = new LineSegment(testHint.getCenter(),
                    testHint.getRight());
            LineSegment testAD = new LineSegment(testRect[0], testRect[3]);
            Coordinate AD_hint = lineWayIntersection(hintLine, testAD);
            if (testHint.getDirection() == up) {
                return i;
            }
            if (testHint.getDirection() == right &&
                    testHint.getUpperHintPoint().getX() < testHint.getLowerHintPoint().getX() &&
                    AD_hint != null) {
                return i;
            }
        }
        throw new IllegalArgumentException("Somehow there was no basic transformation found for this " +
                "rectangle and hint. This is impossible.\n" + "rectangle: " + Arrays.toString(rectangle) +
                " anglepoints: " + hint.getRight() + hint.getCenter());
    }

    /**
     * Inverts the by rectangle defined phi_i, and calculates and returns the inverse points of the points in toTransform.
     *
     * @param i           the index of phi
     * @param rectangle        the rectangle defining phi (and therefore also the inverse of phi)
     * @param toTransform the input for the inverse of phi
     * @return the inverse of phi_i defined by rectangle, applied on toTransform
     */
    static Coordinate[] phiOtherRectangleInverse(int i, Coordinate[] rectangle, Coordinate[] toTransform) {
        assertRectangle(rectangle);
        assertRectangle(toTransform);
        Coordinate[] ret = new Coordinate[]{
                phiPointInverse(i, rectangle, toTransform[0]),
                phiPointInverse(i, rectangle, toTransform[1]),
                phiPointInverse(i, rectangle, toTransform[2]),
                phiPointInverse(i, rectangle, toTransform[3])
        };
        return arrangeRectangle(ret);
    }
}
