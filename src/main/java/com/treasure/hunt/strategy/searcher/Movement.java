package com.treasure.hunt.strategy.searcher;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.geom.HintAndMovement;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a movement of the searcher in the plain,
 * stored as a list of {@link Point} objects.
 *
 * @author dorianreineccius
 */
public class Movement extends HintAndMovement {
    protected List<GeometryItem<?>> additionalGeometryItems = new ArrayList<>();
    /**
     * Earlier added items that are now removed from display
     */

    private List<GeometryItem<Point>> points = new ArrayList<>();

    public Movement() {
    }

    public Movement(Point... points) {
        this.points.addAll(Arrays.stream(points).map(point -> new GeometryItem<>(point, GeometryType.WAY_POINT)).collect(Collectors.toList()));
    }

    /**
     * @return the first points of the moves-sequence.
     */
    public Point getStartingPoint() {
        if (points.size() == 0) {
            throw new IllegalStateException("Movement has size of 0.");
        }
        return points.get(0).getObject();
    }

    /**
     * @return the last end-position of the moves-sequence.
     */
    public Point getEndPoint() {
        if (points.size() == 0) {
            throw new IllegalStateException("Movement has size of 0.");
        }
        return points.get(points.size() - 1).getObject();
    }

    /**
     * @param point The next point, visited in this movement.
     */
    public void addWayPoint(Point point) {
        points.add(new GeometryItem<>(point, GeometryType.WAY_POINT));
        addAdditionalItem(new GeometryItem<>(point, GeometryType.WAY_POINT));
    }

    /**
     * @param geometryItem to add {@link GeometryItem} objects, which are only relevant for displaying
     */
    public void addAdditionalItem(GeometryItem geometryItem) {
        additionalGeometryItems.add(geometryItem);
    }

    public List<GeometryItem<Point>> getPoints() {
        return this.points;
    }

    public List<GeometryItem<?>> getAdditionalGeometryItems() {
        return this.additionalGeometryItems;
    }
}
