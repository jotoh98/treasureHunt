package com.treasure.hunt.strategy.seeker;

import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

import static com.treasure.hunt.strategy.seeker.Seeker.WAY_POINT;

public class Moves extends Product {
    private List<GeometryItem<Point>> points = new ArrayList<>();

    public GeometryItem<Point> getEndPoint() {
        return points.get(points.size() - 1);
    }

    protected void addWayPoint(Point point) {
        points.add(new GeometryItem<>(point, WAY_POINT));
        addAdditionalItem(point, WAY_POINT);
    }
}
