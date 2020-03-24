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
 * The idea is to maintain a square or search area of variable size,
 * given a hint one use Geometry.intersection() to exclude area.
 * Hints need to be transformed into objects of class Geometry. (see createPolygonHintFrom(GeometryAngle hint))
 * At a given moment one can extend the search area for example: area < constant
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

        Geometry polyHint = createPolygonHintFrom(hint.getGeometryAngle());
        if (searchArea.getArea() < Math.pow(2, 8)) {
            extendSearchSquare();
        }
        searchArea = searchArea.intersection(polyHint);
        currentPosition = nextPosition();
        SearchPath currentPath = new SearchPath(currentPosition);
        currentPath.addAdditionalItem(new GeometryItem(searchArea, GeometryType.CURRENT_POLYGON));
        return currentPath;
    }

    /***
     *
     * @param hint a geometry angle which will be used to create a intersectable (Geometry.intersect()) hint for the search area
     * @return a square intersected with given hint. The initiale square is centered at 0,0 and of dim 2*currentSearchFieldDim
     */
    public Geometry createPolygonHintFrom(GeometryAngle hint) {
        List<Coordinate> vertices = new ArrayList<>();
        vertices.addAll(Arrays.stream(createSquare(currentSearchFieldDim * 4).getCoordinates()).distinct().collect(Collectors.toList()));
        vertices.add(vertices.get(0).copy());
        List<Coordinate> polyHint = new ArrayList<>();
        for (int i = 0; i < vertices.size() - 1; i++) {
            LineSegment currentLineSeg = new LineSegment(vertices.get(i), vertices.get(i + 1));

            Coordinate leftInter = hint.leftRay().intersection(currentLineSeg);
            if (leftInter != null && currentLineSeg.distance(leftInter) > 1e-10) {
                leftInter = null;
            }

            Coordinate rightInter = hint.rightRay().intersection(currentLineSeg);
            if (rightInter != null && currentLineSeg.distance(rightInter) > 1e-10) {
                rightInter = null;
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
        /*Coordinate leftExtension = hint.leftVector().multiply(Math.pow(4, curentSearchFieldArea)).translate(hint.getLeft());
        Coordinate rightExtension = hint.rightVector().multiply(Math.pow(4, curentSearchFieldArea)).translate(hint.getRight());
        List<Coordinate> polyEdges = new ArrayList<>();
        polyEdges.addAll(Arrays.asList(hint.getCenter().copy(), rightExtension));
        polyEdges.addAll(
                Arrays.asList(square.getCoordinates()).stream()
                        .filter(hint::inView).distinct()
                        .collect(Collectors.toList()));
        polyEdges.add(leftExtension);
        polyEdges.add(hint.getCenter().copy());
        Polygon hintPoly = JTSUtils.GEOMETRY_FACTORY.createPolygon(polyEdges.toArray(Coordinate[]::new));
        if (hint.extend() >= Math.PI) {
            return square.difference(hintPoly);
        }
        return hintPoly.intersection(square);*/
    }

    /**
     * First, many ideas for next position are possible. this is just one of them!
     * this will head to the interior point (geometric centroid) of the search area, preferable of length 1, otherwise
     * if the searcher is not in the search area the length will be added with a constant until reaching the interior
     *
     * @return the point the instance will next head to
     */
    public Point nextPosition() {
        double length = searchArea.getInteriorPoint().getCoordinate().distance(new Coordinate(0, 0));
        for (double currentScale = 1; currentScale < length; currentScale += length / 20) {
            if (searchArea.contains(
                    JTSUtils.createPoint(
                            JTSUtils.coordinateInDistance(
                                    currentPosition.getCoordinate(),
                                    searchArea.getInteriorPoint().getCoordinate(),
                                    currentScale
                            )
                    )
            )
            ) {
                return JTSUtils.createPoint(
                        JTSUtils.coordinateInDistance(
                                currentPosition.getCoordinate(),
                                searchArea.getInteriorPoint().getCoordinate(),
                                currentScale
                        )
                );
            }
        }
        return searchArea.getInteriorPoint();
    }

    /**
     * extends the current search area by creating a square, doubled dimension of previous square, and intersecting with every previous hint
     */
    public void extendSearchSquare() {
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
