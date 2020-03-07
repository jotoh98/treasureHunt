package com.treasure.hunt.strategy.searcher;


import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.geom.HintAndMovement;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.ListUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.treasure.hunt.utils.JTSUtils.GEOMETRY_FACTORY;

/**
 * This is the path of the searcher in the plain searching the treasure,
 * stored as a list of {@link Point}s.
 *
 * @author dorianreineccius, hassel
 */
@NoArgsConstructor
public class SearchPath extends HintAndMovement {
    @Getter
    protected List<GeometryItem<?>> additional = new ArrayList<>();

    /**
     * The list of points representing the searching path
     * of the corresponding searcher.
     */
    @Getter
    @Setter
    private List<Point> points = new ArrayList<>();

    public SearchPath(Point... points) {
        this(new ArrayList<>(Arrays.asList(points)));
    }

    public SearchPath(List<Point> points) {
        this.points = points;
    }

    public SearchPath(Coordinate... coordinates) {
        for (Coordinate coordinate : coordinates) {
            this.addPoint(GEOMETRY_FACTORY.createPoint(coordinate));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Geometry getGeometry() {
        // TODO implement this more beautiful.
        if (points.size() == 1) {
            return JTSUtils.createPoint(points.get(0).getX(), points.get(0).getY());
        }

        List<Coordinate> l = new LinkedList<>();
        points.forEach(p -> l.add(p.getCoordinate()));
        Coordinate[] coords = new Coordinate[l.size()];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = l.get(i);
        }
        return GEOMETRY_FACTORY.createLineString(coords);
    }

    /**
     * @return the first points of the moves-sequence.
     */
    public Point getFirstPoint() {
        if (points.size() == 0) {
            return null;
        }
        return points.get(0);
    }

    /**
     * @return the last end-position of the moves-sequence.
     */
    public Point getLastPoint() {
        if (points.size() == 0) {
            return null;
        }

        return points.get(points.size() - 1);
    }

    /**
     * @param point The next point, visited in this movement.
     */
    public void addPoint(Point point) {
        if (Double.isNaN(point.getX()) || Double.isNaN(point.getY())) {
            throw new IllegalArgumentException("Point with NAN as coordinate is invalid");
        }
        points.add(point);
    }

    public void addPoint(double x, double y) {
        addPoint(JTSUtils.createPoint(x, y));
    }

    /**
     * @param geometryItem to add {@link GeometryItem} objects, which are only relevant for displaying
     */
    public void addAdditionalItem(GeometryItem<?> geometryItem) {
        additional.add(geometryItem);
    }

    public List<GeometryItem<Point>> getPointList() {
        return points.stream()
                .map(point -> new GeometryItem<>(point, GeometryType.WAY_POINT))
                .collect(Collectors.toList());
    }

    public List<GeometryItem<LineString>> getLines() {
        List<Coordinate> coordinateList = JTSUtils.getCoordinateList(points);

        return ListUtils
                .consecutive(coordinateList, (c1, c2) ->
                        new GeometryItem<>(
                                GEOMETRY_FACTORY.createLineString(new Coordinate[]{c1, c2}),
                                GeometryType.WAY_POINT
                        )
                )
                .collect(Collectors.toList());
    }

    public boolean located(Point pathStart, Point treasure) {
        if (points.size() < 1) {
            return false;
        }

        if (points.size() == 1) {
            return Distance.pointToSegment(
                    treasure.getCoordinate(),
                    pathStart.getCoordinate(),
                    points.get(0).getCoordinate()) <= Searcher.SCANNING_DISTANCE;
        }

        List<Coordinate> wayCoordinates = points.stream()
                .map(Point::getCoordinate)
                .collect(Collectors.toList());

        wayCoordinates.add(0, pathStart.getCoordinate());

        return ListUtils
                .consecutive(wayCoordinates, (firstCoordinate, nextCoordinate) ->
                        Distance.pointToSegment(treasure.getCoordinate(), firstCoordinate, nextCoordinate)
                )
                .anyMatch(distance -> distance <= Searcher.SCANNING_DISTANCE);
    }

    public double getLength() {
        return ListUtils.consecutive(JTSUtils.getCoordinateList(points), Coordinate::distance)
                .reduce(Double::sum)
                .orElse(0d);
    }
}
