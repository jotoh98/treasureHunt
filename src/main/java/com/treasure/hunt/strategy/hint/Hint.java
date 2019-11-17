package com.treasure.hunt.strategy.hint;

import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import org.locationtech.jts.geom.Point;

public class Hint extends Product {
    protected Point center;

    public Hint(Point center) {
        this.center = center;
        addAdditionalItem(new GeometryItem<>(center, GeometryType.HINT_CENTER));
    }
}
