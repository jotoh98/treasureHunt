package com.treasure.hunt.strategy.hint;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import lombok.Getter;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

public abstract class Product {
    @Getter
    protected List<GeometryItem> geometryItems;

    public void addAdditionalItem(Geometry target, GeometryType geometryType) {
        addAdditionalItem(target, geometryType);
    }
}
