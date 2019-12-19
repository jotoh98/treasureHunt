package com.treasure.hunt.strategy.hint;

import com.treasure.hunt.strategy.geom.GeometryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A hint a {@link com.treasure.hunt.strategy.hider.Hider} gives to the {@link com.treasure.hunt.strategy.searcher.Searcher},
 * in order he may find the treasure, the {@link com.treasure.hunt.strategy.hider.Hider} hides.
 *
 * @author jotoh
 */
public abstract class Hint {
    protected List<GeometryItem<?>> additionalGeometryItems = new ArrayList<>();

    /**
     * @param geometryItem to add {@link GeometryItem} objects, which are only relevant for displaying
     */
    public void addAdditionalItem(GeometryItem<?> geometryItem) {
        additionalGeometryItems.add(geometryItem);
    }

    /**
     * @return a list of this hints {@link GeometryItem} objects.
     */
    public abstract List<GeometryItem<?>> getGeometryItems();

    public List<GeometryItem<?>> getAdditionalGeometryItems() {
        return this.additionalGeometryItems;
    }

    public void setAdditionalGeometryItems(List<GeometryItem<?>> additionalGeometryItems) {
        this.additionalGeometryItems = additionalGeometryItems;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Hint)) {
            return false;
        }
        final Hint other = (Hint) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$additionalGeometryItems = this.getAdditionalGeometryItems();
        final Object other$additionalGeometryItems = other.getAdditionalGeometryItems();
        return this$additionalGeometryItems == null ? other$additionalGeometryItems == null : this$additionalGeometryItems.equals(other$additionalGeometryItems);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Hint;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $additionalGeometryItems = this.getAdditionalGeometryItems();
        result = result * PRIME + ($additionalGeometryItems == null ? 43 : $additionalGeometryItems.hashCode());
        return result;
    }

    public String toString() {
        return "Hint(additionalGeometryItems=" + this.getAdditionalGeometryItems() + ")";
    }
}
