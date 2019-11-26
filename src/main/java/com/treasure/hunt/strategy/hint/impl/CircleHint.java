package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.jts.Circle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.utils.JTSUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class CircleHint extends Hint {
    private Point center;
    private double radius;

    public List<GeometryItem> getGeometryItems() {
        List<GeometryItem> output = new ArrayList<>();
        output.add(new GeometryItem(
                new Circle(center.getCoordinate(), radius, JTSUtils.GEOMETRY_FACTORY)
                , GeometryType.HINT_CENTER));
        return output;
    }
}
