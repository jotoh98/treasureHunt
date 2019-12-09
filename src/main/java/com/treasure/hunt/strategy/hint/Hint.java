package com.treasure.hunt.strategy.hint;

import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * A hint a {@link com.treasure.hunt.strategy.hider.Hider} gives to the {@link com.treasure.hunt.strategy.searcher.Searcher},
 * in order he may find the treasure, the {@link com.treasure.hunt.strategy.hider.Hider} hides.
 *
 * @author hassel
 */
@Data
public abstract class Hint {
    protected List<GeometryItem> additionalGeometryItems = new ArrayList<>();

    /**
     * @param geometryItem to add {@link GeometryItem} objects, which are only relevant for displaying
     */
    public void addAdditionalItem(GeometryItem geometryItem) {
        additionalGeometryItems.add(geometryItem);
    }

    /**
     * @return a list of this hints {@link GeometryItem} objects.
     */
    public abstract List<GeometryItem> getGeometryItems();
}
