package com.treasure.hunt.strategy;

import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class Product {
    @Getter
    protected List<GeometryItem> geometryItems = new ArrayList<>();

    public void addAdditionalItem(GeometryItem geometryItem) {
        geometryItems.add(geometryItem);
    }
}
