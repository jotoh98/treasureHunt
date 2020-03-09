package com.treasure.hunt.strategy.searcher;


import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.geom.HintAndMovement;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.ListUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
 * @author dorianreineccius
 */
@NoArgsConstructor
public class SearchPathPrototype extends HintAndMovement {
    @Getter
    protected List<GeometryItem<?>> additional = new ArrayList<>();

    /**
     * The list of points representing the searching path
     * of the corresponding searcher.
     */
    @Getter
    @Setter
    private List<Coordinate> coordinates = new ArrayList<>();

    public SearchPathPrototype(Point... coordinates) {
        this.coordinates = Arrays.asList(coordinates).stream()
                .map(point -> point.getCoordinate())
                .collect(Collectors.toList());
    }

    public SearchPathPrototype(Coordinate... coordinates) {
        this.coordinates = Arrays.asList(coordinates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Geometry getGeometry() {
        // TODO implement this more beautiful.
        if (coordinates.size() == 1) {
            return JTSUtils.createPoint(coordinates.get(0));
        }

        List<Coordinate> l = new LinkedList<>();
        coordinates.forEach(p -> l.add(p));
        Coordinate[] coords = new Coordinate[l.size()];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = l.get(i);
        }
        return GEOMETRY_FACTORY.createLineString(coords);
    }

    /**
     * @param point The next point, visited in this movement.
     */
    public void addPoint(Point point) {
        addPoint(point.getCoordinate());
    }

    public void addPoint(Coordinate coordinate) {
        if (Double.isNaN(coordinate.x) || Double.isNaN(coordinate.y)) {
            throw new IllegalArgumentException("Point with NAN as coordinate is invalid");
        }
        coordinates.add(coordinate);
    }

    /**
     * @param geometryItem to add {@link GeometryItem} objects, which are only relevant for displaying
     */
    public void addAdditionalItem(GeometryItem<?> geometryItem) {
        additional.add(geometryItem);
    }

    public List<GeometryItem<LineString>> getLinesGeometryItemsList() {
        return ListUtils
                .consecutive(coordinates, (c1, c2) ->
                        new GeometryItem<>(
                                GEOMETRY_FACTORY.createLineString(new Coordinate[]{c1, c2}),
                                GeometryType.WAY_POINT
                        )
                )
                .collect(Collectors.toList());
    }

    public List<Point> getPoints() {
        return this.coordinates.stream()
                .map(coordinate -> JTSUtils.createPoint(coordinate))
                .collect(Collectors.toList());
    }
}
