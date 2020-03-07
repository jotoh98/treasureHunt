package com.treasure.hunt.strategy.searcher;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.ListUtils;
import lombok.Getter;
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
public class SearchPath extends SearchPathPrototype {
    @Getter
    protected List<GeometryItem<?>> additional = new ArrayList<>();

    /**
     * The list of points representing the searching path
     * of the corresponding searcher.
     */
    @Getter
    private final List<Point> points = new ArrayList<>(); // TODO Coordinates
    @Getter
    private final Coordinate searcherStart;
    @Getter
    private final Coordinate searcherEnd;

    public SearchPath(Point... points) { // TODO remove
        this(new ArrayList<>(Arrays.asList(points)));
    }

    public SearchPath(List<Point> points) { // TODO remove
        if (points.isEmpty()) {
            throw new IllegalArgumentException("SearchPath must get initialized with ≥1 points!");
        }
        searcherStart = points.get(0).getCoordinate();
        searcherEnd = points.get(points.size() - 1).getCoordinate();
        if (points.size() > 1) {
            this.points.addAll(points);
        }
    }

    public SearchPath(Coordinate... coordinates) {
        if (coordinates.length == 0) {
            throw new IllegalArgumentException("SearchPath must get initialized with ≥1 coordinates!");
        }
        searcherStart = coordinates[0];
        searcherEnd = coordinates[coordinates.length - 1];
        if (coordinates.length > 1) {
            for (Coordinate coordinate : coordinates) {
                this.addPoint(GEOMETRY_FACTORY.createPoint(coordinate));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Geometry getGeometry() {
        // TODO implement this more beautiful.
        List<Coordinate> l = new LinkedList<>();
        points.forEach(p -> l.add(p.getCoordinate()));
        Coordinate[] coords = new Coordinate[l.size()];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = l.get(i);
        }
        return GEOMETRY_FACTORY.createLineString(coords);
    }

    /**
     * @param point The next point, visited in this movement.
     */
    public void addPoint(Point point) { // TODO remove
        if (Double.isNaN(point.getX()) || Double.isNaN(point.getY())) {
            throw new IllegalArgumentException("Point with NAN as coordinate is invalid");
        }
        points.add(point);
    }

    public void addPoint(double x, double y) {// TODO remove
        addPoint(JTSUtils.createPoint(x, y));
    }

    /**
     * @param geometryItem to add {@link GeometryItem} objects, which are only relevant for displaying
     */
    public void addAdditionalItem(GeometryItem<?> geometryItem) { // TODO remove
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

    public double getLength() {
        return ListUtils.consecutive(JTSUtils.getCoordinateList(points), Coordinate::distance)
                .reduce(Double::sum)
                .orElse(0d);
    }

    public Point getSearcherStartPoint() {
        return JTSUtils.createPoint(searcherStart.x, searcherStart.y);
    }

    public Point getSearcherEndPoint() {
        return JTSUtils.createPoint(searcherEnd.x, searcherEnd.y);
    }
}
