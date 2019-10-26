package com.treasure.hunt.strategy.hint;

import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.Getter;
import lombok.Setter;


public class Hint extends Product {
    @Getter
    @Setter
    GeometryItem globalTarget;
}
