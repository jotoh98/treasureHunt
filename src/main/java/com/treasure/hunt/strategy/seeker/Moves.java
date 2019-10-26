package com.treasure.hunt.strategy.seeker;

import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class Moves extends Product {
    private List<GeometryItem<Point>> points = new ArrayList<>();

    public GeometryItem<Point> getEndPoint() {
        return points.get(points.size() - 1);
    }

    protected void addWayPoint(Point point) {
        points.add(new GeometryItem<>(point, GeometryType.WAY_POINT));
        addAdditionalItem(point, GeometryType.WAY_POINT);
    }
}
