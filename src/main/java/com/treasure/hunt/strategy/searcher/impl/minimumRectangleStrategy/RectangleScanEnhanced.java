package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper.meanderThroughLines;

/**
 * Implements some alternatives to the RectangleScan-Routine used by the paper "Deterministic Treasure Hunt in the
 * Plane with Angular Hints" from Bouchard et al.
 *
 * @author Rank
 */
public class RectangleScanEnhanced {
    MinimumRectangleSearcher strategy;

    public RectangleScanEnhanced(MinimumRectangleSearcher strategy) {
        this.strategy = strategy;
    }

    /**
     * Meanders through the rectangle to scan it like the RectangleScan Routine from the paper but uses fewer distance
     *
     * @param a    a corner of the rectangle which is to be scanned, neighboring d and b
     * @param b    a corner of the rectangle which is to be scanned, neighboring a and c
     * @param c    a corner of the rectangle which is to be scanned, neighboring b and d
     * @param d    a corner of the rectangle which is to be scanned, neighboring c and a
     * @param move the search-path the scan should be added to
     * @return the resulting search-path
     */
    public static SearchPath rectangleScanEnhanced(Coordinate a, Coordinate b, Coordinate c, Coordinate d,
                                                   SearchPath move) {
        if (a.distance(b) > a.distance(d)) {
            Coordinate temp = a;
            a = d;
            d = c;
            c = b;
            b = temp;
        }
        int numberOfPointsInOneLine = (int) Math.ceil(a.distance(b) / 2);
        if (numberOfPointsInOneLine == 1) {
            Vector2D aToBHalf = new Vector2D(a, b);
            aToBHalf = aToBHalf.divide(2);
            move.addPoint(JTSUtils.createPoint(a.x + aToBHalf.getX()
                    , a.y + aToBHalf.getY()));
            move.addPoint(JTSUtils.createPoint(d.x + aToBHalf.getX(),
                    d.y + aToBHalf.getY()
            ));
            return move;
        }

        Point[] a_k = lineOfPointsWithDistanceAtMostTwo(numberOfPointsInOneLine, a, b);
        Point[] b_k = lineOfPointsWithDistanceAtMostTwo(numberOfPointsInOneLine, d, c);
        return meanderThroughLines(a_k, b_k, numberOfPointsInOneLine - 1, move);
    }

    /**
     * Returns a list of points on the line-segment from p1 to p2
     * The first point is in distance 1 to p1 and the last point is in distance 1 to p2.
     * The other points go consecutive from the first to the last point and have equal distances to their neighboring
     * points. There are numberOfPointsOnLine points in this returned list.
     *
     * @param numberOfPointsOnLine the number of points in the returned list
     */
    static private Point[] lineOfPointsWithDistanceAtMostTwo(int numberOfPointsOnLine, Coordinate p1, Coordinate p2) {
        if (numberOfPointsOnLine <= 1) {
            throw new IllegalArgumentException("numberOfPointsOnLine must be bigger than 1 but equals " + numberOfPointsOnLine);
        }
        Vector2D p1ToP2WithDistanceOne = new Vector2D(p1, p2);
        p1ToP2WithDistanceOne = p1ToP2WithDistanceOne.divide(p1ToP2WithDistanceOne.length());

        Coordinate newP1 = new Coordinate(p1.x + p1ToP2WithDistanceOne.getX(),
                p1.y + p1ToP2WithDistanceOne.getY());
        Coordinate newP2 = new Coordinate(p2.x - p1ToP2WithDistanceOne.getX(),
                p2.y - p1ToP2WithDistanceOne.getY());

        Point[] res = new Point[numberOfPointsOnLine + 1];

        double xDist = newP2.getX() - newP1.getX();
        double yDist = newP2.getY() - newP1.getY();
        for (int i = 0; i < numberOfPointsOnLine; i++) {
            res[i] = JTSUtils.createPoint(newP1.getX() + xDist * ((double) i / (double) (numberOfPointsOnLine - 1)),
                    newP1.getY() + yDist * ((double) i / (double) (numberOfPointsOnLine - 1)));
        }
        return res;
    }

    /**
     * Replaces RectangleScan and does so by only scanning the minimum to the rectangle abcd parallel rectangle
     * which covers all areas not seen and not excludable by hints, inside the current phase rectangle and the input rectangle.
     * It then uses EnhancedRectangleScan to scan this rectangle.
     *
     * @param a    a corner of the rectangle which is to be scanned, neighboring d and b
     * @param b    a corner of the rectangle which is to be scanned, neighboring a and c
     * @param c    a corner of the rectangle which is to be scanned, neighboring b and d
     * @param d    a corner of the rectangle which is to be scanned, neighboring c and a
     * @param move the search-path the scan should be added to
     * @return the resulting search-path
     */
    SearchPath rectangleScanMinimal(Coordinate a, Coordinate b,
                                    Coordinate c, Coordinate d, SearchPath move) {
        TransformForAxisParallelism transformerForRectangleAxisParallelism =
                new TransformForAxisParallelism(new LineSegment(a, b));
        strategy.updateVisitedPolygon(move);
        Polygon rectanglePolygon = JTSUtils.GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
                a, b, c, d, a});

        Geometry newAreaToScan = strategy.getCurrentMultiPolygon().intersection(rectanglePolygon);

        if (newAreaToScan == null || newAreaToScan.getArea() == 0) {
            return move;
        }
        Geometry newAreaToScanTransformed = transformerForRectangleAxisParallelism.toInternal(newAreaToScan);
        if (newAreaToScanTransformed.getArea() == 0) {
            return move;
        }

        Coordinate[] envelopeToScanTransformedPoints = newAreaToScanTransformed.getEnvelope().getCoordinates();

        return rectangleScanEnhanced(
                transformerForRectangleAxisParallelism.toExternal(envelopeToScanTransformedPoints[0]),
                transformerForRectangleAxisParallelism.toExternal(envelopeToScanTransformedPoints[1]),
                transformerForRectangleAxisParallelism.toExternal(envelopeToScanTransformedPoints[2]),
                transformerForRectangleAxisParallelism.toExternal(envelopeToScanTransformedPoints[3]), move
        );
    }
}
