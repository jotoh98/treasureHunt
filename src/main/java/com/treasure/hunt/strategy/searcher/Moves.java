package com.treasure.hunt.strategy.searcher;

import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class Moves extends Product {

    @Getter
    private List<GeometryItem<Point>> points = new ArrayList<>();

    /**
     * @return the last end-position of the moves-sequence.
     */
    public GeometryItem<Point> getEndPoint() {
        return points.get(points.size() - 1);
    }

    public void addWayPoint(Point point) {
        points.add(new GeometryItem<>(point, GeometryType.WAY_POINT));
        addAdditionalItem(new GeometryItem<>(point, GeometryType.WAY_POINT));
    }
}
