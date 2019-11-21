package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import lombok.Getter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

@ToString(of = {"centerPoint", "anglePointLeft", "anglePointRight"})
@Getter
public class AngleHint extends Hint {
    Point anglePointRight;
    Point center;
    Point anglePointLeft;

    public AngleHint(Point anglePointRight, Point center, Point anglePointLeft) {
        this.anglePointRight = anglePointRight;
        this.center = center;
        this.anglePointLeft = anglePointLeft;
    }

    public List<GeometryItem> getGeometryItems() {
        List<GeometryItem> output = new ArrayList<>();
        output.add(new GeometryItem(center, GeometryType.HINT_CENTER));
        output.add(new GeometryItem(anglePointLeft, GeometryType.HINT_ANGLE));
        output.add(new GeometryItem(anglePointRight, GeometryType.HINT_ANGLE));
        return output;
    }
}
