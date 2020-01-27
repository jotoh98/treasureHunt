package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dorianreineccius, jotoh
 */
@Getter
@Slf4j
public class AngleHint extends Hint {
    GeometryAngle geometryAngle;

    public AngleHint(Coordinate left, Coordinate center, Coordinate right) {
        this(new GeometryAngle(JTSUtils.GEOMETRY_FACTORY, left, center, right));
    }

    public AngleHint(GeometryAngle angle) {
        geometryAngle = angle;
        log.trace(angle.toString());
    }

    public List<GeometryItem<?>> getGeometryItems() {
        List<GeometryItem<?>> output = new ArrayList<>();
        output.add(new GeometryItem<>(geometryAngle, GeometryType.HINT_ANGLE));
        return output;
    }
}
