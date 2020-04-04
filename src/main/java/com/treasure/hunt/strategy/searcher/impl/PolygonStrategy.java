package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The idea is to maintain a square called search area of variable size,
 * given a hint one use Geometry.intersection() to exclude area.
 * Hints need to be transformed into objects of class Geometry. (see createPolygonHintFrom(GeometryAngle hint))
 * At a given moment one can extend the search area for example: area smaller constant
 * the next position will be calculated dependent of the interior point of the search area (see nextPosition())
 */

@Slf4j
public class PolygonStrategy implements Searcher<AngleHint> {

    Geometry searchArea;
    double currentSearchFieldDim = 4;
    Point currentPosition;
    List<GeometryAngle> hints = new ArrayList<>();

    @Override
    public void init(Point searcherStartPosition) {
        currentPosition = searcherStartPosition;
        searchArea = createSquare(currentSearchFieldDim);
    }

    @Override
    public SearchPath move() {
        return new SearchPath(currentPosition);
    }

    @Override
    public SearchPath move(AngleHint hint) {
        hints.add(hint.getGeometryAngle().copy());

        Envelope envelope = searchArea.getEnvelopeInternal();
        Geometry polyHint = createPolygonHintFrom(hint.getGeometryAngle());
        if ((searchArea.getArea() < Math.pow(2, 4) || envelope.getWidth() < 2 || envelope.getHeight() < 2)) {
            extendSearchSquare();
            envelope = searchArea.getEnvelopeInternal();
            if (envelope.getWidth() < 1.1 || envelope.getHeight() < 1.1) {
                extendSearchSquare();

                return scanCompleteSearchArea();
            }
        }
        searchArea = searchArea.intersection(polyHint);
        currentPosition = nextPosition();
        SearchPath currentPath = new SearchPath(currentPosition);
        currentPath.addAdditionalItem(new GeometryItem(searchArea, GeometryType.CURRENT_POLYGON));
        return currentPath;
    }

    /**
     * if the search area is too tight (height smaller 2) and bounded than every vertices will be visited
     *
     * @return SearchPath including every vertices of the search area
     */
    public SearchPath scanCompleteSearchArea() {
        SearchPath currentPath = new SearchPath(currentPosition);
        currentPath.addAdditionalItem(new GeometryItem(searchArea, GeometryType.CURRENT_POLYGON));
        for (Coordinate vertices : searchArea.getCoordinates()) {
            currentPath.addPoint(JTSUtils.createPoint(vertices));
        }

        currentPosition = currentPath.getLastPoint();

        return currentPath;
    }

    /***
     *
     * @param hint a geometry angle which will be used to create a intersectable (Geometry.intersect()) hint for the search area
     * @return a square intersected with given hint. The initial square is centered at 0,0 and of dim 2*currentSearchFieldDim
     */
    public Geometry createPolygonHintFrom(GeometryAngle hint) {
        List<Coordinate> vertices = new ArrayList<>(Arrays.stream(createSquare(currentSearchFieldDim * 4).getCoordinates()).distinct().collect(Collectors.toList()));
        vertices.add(vertices.get(0).copy());
        List<Coordinate> polyHint = new ArrayList<>();
        for (int i = 0; i < vertices.size() - 1; i++) {
            LineSegment currentLineSeg = new LineSegment(vertices.get(i), vertices.get(i + 1));

            Coordinate leftInter = hint.leftRay().intersection(currentLineSeg);
            if (leftInter != null) {
                if (JTSUtils.coordinateEqual(leftInter, vertices.get(i)) || JTSUtils.coordinateEqual(leftInter, vertices.get(i + 1)) || currentLineSeg.distance(leftInter) > 1e-10) {
                    leftInter = null;
                }
            }

            Coordinate rightInter = hint.rightRay().intersection(currentLineSeg);
            if (rightInter != null) {
                if (JTSUtils.coordinateEqual(rightInter, vertices.get(i)) || JTSUtils.coordinateEqual(rightInter, vertices.get(i + 1)) || currentLineSeg.distance(rightInter) > 1e-10) {
                    rightInter = null;
                }
            }

            if (hint.inView(vertices.get(i))) {
                polyHint.add(vertices.get(i).copy());
            }

            if (leftInter != null) {

                if (rightInter != null) {
                    polyHint.add(rightInter);
                    polyHint.add(hint.getCenter().copy());
                }
                polyHint.add(leftInter);
            } else {
                if (rightInter != null) {
                    polyHint.add(rightInter);
                    polyHint.add(hint.getCenter().copy());

                }
            }
        }
        polyHint.add(polyHint.get(0).copy());
        return JTSUtils.GEOMETRY_FACTORY.createPolygon(polyHint.toArray(Coordinate[]::new));
    }

    /**
     * First, many ideas for next position are possible. this is just one of them!
     * this will head to the interior point (geometric centroid) of the search area, preferable of length 1, otherwise
     * if the searcher is not in the search area the length will be added with a constant until reaching the interior
     *
     * @return the point the instance will next head to
     */
    public Point nextPosition() {
        return JTSUtils.createPoint(JTSUtils.coordinateInDistance(currentPosition.getCoordinate(), searchArea.getInteriorPoint().getCoordinate(), 1));
    }

    /**
     * extends the current search area by creating a square, doubled dimension of previous square, and intersecting with every previous hint
     */
    public void extendSearchSquare() {
        if (currentSearchFieldDim >= Math.pow(2, 19)) {
            return;
        }
        currentSearchFieldDim = 2 * currentSearchFieldDim;
        Geometry nextSquare = createSquare(currentSearchFieldDim);
        nextSquare = intersectWithPrevious(hints, nextSquare);
        searchArea = nextSquare;
    }

    /**
     * @param dimension the side length of the square
     * @return square of side length dimension
     */
    public Polygon createSquare(double dimension) {
        return JTSUtils.GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
                new Coordinate(dimension, dimension),
                new Coordinate(dimension, -dimension),
                new Coordinate(-dimension, -dimension),
                new Coordinate(-dimension, dimension),
                new Coordinate(dimension, dimension)});
    }

    /**
     * @param previousHints List of all previous hints
     * @param square        the to be intersected square with all previous hints
     * @return a square intersected with all previous hints
     */
    public Geometry intersectWithPrevious(List<GeometryAngle> previousHints, Geometry square) {
        Geometry result = square.copy();
        for (GeometryAngle hint : previousHints) {
            Geometry currentIntersect = createPolygonHintFrom(hint);
            result = result.intersection(currentIntersect);
        }
        return result;
    }
}
