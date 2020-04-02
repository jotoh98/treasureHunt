package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

import static com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.RoutinesFromPaper.meanderThroughLines;

/**
 * @author Rank
 */
@Slf4j
public class RectangleScanEnhanced {
    MinimumRectangleStrategy strategy;

    public RectangleScanEnhanced(MinimumRectangleStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Meanders through the rectangle to scan it like the RectangleScan Routine from the paper but uses fewer distance
     */
    public static SearchPath rectangleScanEnhanced(Coordinate a, Coordinate b, Coordinate c, Coordinate d,
                                                   SearchPath searchPath) {
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
            searchPath.addPoint(JTSUtils.createPoint(a.x + aToBHalf.getX()
                    , a.y + aToBHalf.getY()));
            searchPath.addPoint(JTSUtils.createPoint(d.x + aToBHalf.getX(),
                    d.y + aToBHalf.getY()
            ));
            return searchPath;
        }

        Point[] a_k = lineOfPointsWithDistanceAtMostTwo(numberOfPointsInOneLine, a, b);
        Point[] b_k = lineOfPointsWithDistanceAtMostTwo(numberOfPointsInOneLine, d, c);
        return meanderThroughLines(a_k, b_k, numberOfPointsInOneLine - 1, searchPath);
    }

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

    SearchPath rectangleScanMinimal(Coordinate rectangleCorner1, Coordinate rectangleCorner2,
                                    Coordinate rectangleCorner3, Coordinate rectangleCorner4, SearchPath move) {
        //log.debug("rectangleScanMinimal of " + (rectangleCorner1) + ", " + (rectangleCorner2) + ", "
        //+ (rectangleCorner3) + ", " + (rectangleCorner4) + ", ");

        TransformForAxisParallelism transformerForRectangleAxisParallelism =
                new TransformForAxisParallelism(new LineSegment(rectangleCorner1, rectangleCorner2));
        strategy.updateVisitedPolygon(move);
        //Polygon newAreaToScan = intersectHints(strategy.getObtainedHints(), rectangleToScanHints);
        Polygon rectanglePolygon = JTSUtils.GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
                rectangleCorner1, rectangleCorner2, rectangleCorner3, rectangleCorner4, rectangleCorner1});
        //Polygon newAreaToScan = reduceConvexPolygon(rectanglePolygon, strategy.getObtainedHints());

        /*
        Geometry[] polygonsToIntersect = new Geometry[(strategy.getCurrentMultiPolygon().getNumGeometries() + 1)];
        for (int i = 0; i < strategy.getCurrentMultiPolygon().getNumGeometries(); i++) {
            polygonsToIntersect[i] = strategy.getCurrentMultiPolygon().getGeometryN(i);
        }
        polygonsToIntersect[polygonsToIntersect.length - 1] = rectanglePolygon;
        Geometry newAreaToScan = JTSUtils.GEOMETRY_FACTORY.createGeometryCollection(polygonsToIntersect);
        newAreaToScan = UnaryUnionOp.union(newAreaToScan);*/
        Geometry newAreaToScan = strategy.getCurrentMultiPolygon().intersection(rectanglePolygon);

        //Polygon newAreaToScan = intersectHints(new ArrayList<>(), rectangleToScanHints);
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
