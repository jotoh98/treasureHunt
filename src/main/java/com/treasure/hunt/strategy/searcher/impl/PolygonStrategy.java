package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.jts.geom.Line;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PolygonStrategy implements Searcher<AngleHint> {


    Polygon square;
    Geometry searchArea;
    double curentSearchFieldArea = 4;
    Point currentPosition;

    @Override
    public void init(Point searcherStartPosition) {
        currentPosition = searcherStartPosition;
        square = createSquare(curentSearchFieldArea);
        searchArea = square;
    }

    @Override
    public SearchPath move() {
        return new SearchPath(currentPosition);
    }

    @Override
    public SearchPath move(AngleHint hint) {
        Geometry polyHint = createPolygonHintFrom(hint.getGeometryAngle());
        if (searchArea.getArea() < 10) {
            extendSearchSquare();
        }
        searchArea = searchArea.intersection(polyHint);

        SearchPath currentPath = new SearchPath(nextPosition());
        currentPath.addAdditionalItem(new GeometryItem(searchArea, GeometryType.CURRENT_POLYGON));
        return currentPath;
    }

    public Geometry createPolygonHintFrom(GeometryAngle hint) {
        List<Coordinate> vertices = new ArrayList<>();
        vertices.addAll(Arrays.stream(createSquare(curentSearchFieldArea * 2).getCoordinates()).distinct().collect(Collectors.toList()));
        List<Coordinate> polyHint = new ArrayList<>();
        for (int i = 0; i < vertices.size() - 1; i++) {
            LineSegment currentLineSeg = new LineSegment(vertices.get(i), vertices.get(i + 1));
            Coordinate leftInter = hint.leftRay().intersection(currentLineSeg);
            Coordinate rightInter = hint.rightRay().intersection(currentLineSeg);

            if (hint.inView(vertices.get(i))) {
                polyHint.add(vertices.get(i).copy());
            }

            if (leftInter != null) {
                polyHint.add(leftInter);
            }

            if (rightInter != null) {
                polyHint.add(rightInter);
                polyHint.add(hint.getCenter().copy());

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

    public Point nextPosition() {
        return searchArea.getInteriorPoint();
    }

    public void extendSearchSquare() {
        curentSearchFieldArea = 2 * curentSearchFieldArea;
        square = createSquare(curentSearchFieldArea);
    }

    public Polygon createSquare(double area) {
        return JTSUtils.GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
                new Coordinate(area, area),
                new Coordinate(area, -area),
                new Coordinate(-area, -area),
                new Coordinate(-area, area),
                new Coordinate(area, area)});
    }
}
