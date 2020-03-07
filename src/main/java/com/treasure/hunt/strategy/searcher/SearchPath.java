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
    private final List<GeometryItem<?>> additionalGeometryItemsList = new ArrayList<>();

    /**
     * The list of points representing the searching path
     * of the corresponding searcher.
     */
    @Getter
    private final List<Coordinate> coordinates = new ArrayList<>(); // TODO Coordinates
    @Getter
    private final Coordinate searcherStart;
    @Getter
    private final Coordinate searcherEnd;

    public SearchPath(List<Coordinate> coordinates) {
        if (coordinates.isEmpty()) {
            throw new IllegalArgumentException("SearchPath must get initialized with ≥1 points!");
        }
        searcherStart = coordinates.get(0);
        searcherEnd = coordinates.get(coordinates.size() - 1);
        if (coordinates.size() > 1) {
            this.coordinates.addAll(coordinates);
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
                this.coordinates.add(coordinate);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Geometry getGeometry() {
        Coordinate[] coords = new Coordinate[this.coordinates.size()];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = (Coordinate) this.coordinates.get(i).clone();
        }
        return GEOMETRY_FACTORY.createLineString(coords);
    }

    public List<GeometryItem<Point>> getPointGeometryItemsList() {
        return getPoints().stream()
                .map(point -> new GeometryItem<>(point, GeometryType.WAY_POINT))
                .collect(Collectors.toList());
    }

    public List<GeometryItem<LineString>> getLinesGeometryItemsList() {
        List<Coordinate> coordinateList = JTSUtils.getCoordinateList(getPoints());

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
        return ListUtils.consecutive(JTSUtils.getCoordinateList(getPoints()), Coordinate::distance)
                .reduce(Double::sum)
                .orElse(0d);
    }

    public Point getSearcherStartPoint() {
        return JTSUtils.createPoint(searcherStart);
    }

    public Point getSearcherEndPoint() {
        return JTSUtils.createPoint(searcherEnd);
    }

    public List<Point> getPoints() {
        return coordinates.stream()
                .map(coordinate -> JTSUtils.createPoint(coordinate))
                .collect(Collectors.toList());
    }

    public void addAdditionalItem(GeometryItem geometryItem) {
        this.additionalGeometryItemsList.add(geometryItem);
    }

    public List<GeometryItem<?>> getAdditional() {
        return this.additionalGeometryItemsList;
    }
}
