package com.treasure.hunt.strategy.hint;

import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import lombok.Data;
import org.locationtech.jts.geom.Point;

@Data
public class Hint extends Product {
    protected Point center;

    public Hint(Point center) {
        this.center = center;
        addAdditionalItem(new GeometryItem<>(center, GeometryType.HINT_CENTER));
    }
}
