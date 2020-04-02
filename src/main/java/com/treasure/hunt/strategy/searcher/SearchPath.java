package com.treasure.hunt.strategy.searcher;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.geom.HintAndMovement;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.ListUtils;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the path of the searcher in the plain searching the treasure,
 * stored as a list of {@link Point}s.
 *
 * @author dorianreineccius, hassel
 */
public class SearchPath extends HintAndMovement {
    /**
     * The list of points representing the searching path of the corresponding searcher.
     */
    @Getter
    @Setter
    private List<Point> points;
    /**
     * The list of additional items which should be displayed with this SearchPath.
     */
    @Getter
    private List<GeometryItem<?>> additional = new ArrayList<>();

    public SearchPath(Point... points) {
        this.points = new ArrayList<>(Arrays.asList(points));
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
     * @param point The next {@link Point}, visited in this movement.
     */
    public void addPoint(Point point) {
        points.add(point);
    }

    /**
     * @param point the {@link Point}, the {@link Searcher} starts its movement from.
     */
    public void addPointToFront(Point point) {
        points.add(0, point);
    }

    /**
     * @param geometryItem to add {@link GeometryItem} objects, which are only relevant for displaying
     */
    public void addAdditionalItem(GeometryItem<?> geometryItem) {
        additional.add(geometryItem);
    }

    /**
     * @return a list, containing every {@link Point} of this SearchPath, except the first.
     * List could be empty.
     * @throws IllegalStateException if this SearchPath contains zero {@link Point}s.
     */
    public List<GeometryItem<Point>> getPointsExceptTheFirst() {
        if (points.size() < 1) {
            throw new IllegalStateException("The SearchPath should never got zero points!");
        }

        if (points.size() == 1) {
            return Collections.emptyList();
        }

        return points.subList(1, points.size()).stream()
                .map(point -> new GeometryItem<>(point, GeometryType.WAY_POINT))
                .collect(Collectors.toList());

    }

    /**
     * @return A list of {@link GeometryItem}s, each containing a {@link LineString}. This describes the movement of the {@link Searcher}.
     * @throws IllegalStateException if this searchPath contains zero {@link Point}s.
     */
    public List<GeometryItem<LineString>> getLineGeometryItems() {
        return getLines().stream()
                .map(lineString -> new GeometryItem<>(lineString, GeometryType.WAY_POINT_LINE))
                .collect(Collectors.toList());
    }

    /**
     * @return A list of {@link LineString}, describing the movement of the {@link Searcher}.
     * @throws IllegalStateException if this searchPath contains zero {@link Point}s.
     */
    public List<LineString> getLines() {
        if (points.size() < 1) {
            throw new IllegalStateException("The SearchPath should never got zero points!");
        }

        if (points.size() == 1) {
            return Collections.emptyList();
        }

        return ListUtils
                .consecutive(JTSUtils.getCoordinateList(points), (c1, c2) ->
                        JTSUtils.GEOMETRY_FACTORY.createLineString(new Coordinate[]{c1, c2})
                )
                .collect(Collectors.toList());
    }

    /**
     * @return the distance, of this searchPath.
     */
    public double getLength() {
        return ListUtils.consecutive(JTSUtils.getCoordinateList(points), Coordinate::distance)
                .reduce(Double::sum)
                .orElse(0d);
    }
}
