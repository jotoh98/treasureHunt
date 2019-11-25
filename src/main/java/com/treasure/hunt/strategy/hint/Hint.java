package com.treasure.hunt.strategy.hint;

import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class Hint {
    protected List<GeometryItem> additionalGeometryItems = new ArrayList<>();

    public void addAdditionalItem(GeometryItem geometryItem) {
        additionalGeometryItems.add(geometryItem);
    }

    public abstract List<GeometryItem> getGeometryItems();
}
