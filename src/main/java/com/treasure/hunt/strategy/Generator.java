package com.treasure.hunt.strategy;

import com.treasure.hunt.strategy.geom.GeometryType;

import java.util.List;

public abstract class Generator {

    public abstract List<GeometryType> getAvailableVisualisationGeometryTypes();

    public abstract String getDisplayName();
}
