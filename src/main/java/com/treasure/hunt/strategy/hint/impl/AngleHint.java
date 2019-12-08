package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.geom.Angle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Getter;
import lombok.ToString;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

@ToString(of = {"centerPoint", "anglePointLeft", "anglePointRight"})
@Getter
public class AngleHint extends Hint {
    Angle geometryAngle;

    public AngleHint(Coordinate center, Coordinate anglePointLeft, Coordinate anglePointRight) {
        geometryAngle = new Angle(JTSUtils.GEOMETRY_FACTORY, center, anglePointLeft, anglePointRight);
        System.out.println(geometryAngle);
    }

    public List<GeometryItem> getGeometryItems() {
        List<GeometryItem> output = new ArrayList<>();
        output.add(new GeometryItem(geometryAngle, GeometryType.HINT_ANGLE));
        return output;
    }
}
