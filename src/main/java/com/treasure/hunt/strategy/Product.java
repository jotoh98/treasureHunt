package com.treasure.hunt.strategy;

import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class Product {
    @Getter
    protected List<GeometryItem> geometryItems = new ArrayList<>();

    /**
     * @return the last end-position of the moves-sequence.
     */
    public GeometryItem getEndPoint() {
        assert (geometryItems.size() != 0);
        return geometryItems.get(geometryItems.size() - 1);
    }

    public void addAdditionalItem(GeometryItem geometryItem) {
        geometryItems.add(geometryItem);
    }
}
