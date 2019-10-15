package com.treasure.hunt.strategy.visualisation;

import lombok.Value;
import org.locationtech.jts.geom.Geometry;

@Value
public class VisualisationGeometryItem {
    Geometry geometry;
    VisualisationGeometryType type;
}
