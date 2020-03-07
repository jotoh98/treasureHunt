package com.treasure.hunt.game;

import com.treasure.hunt.jts.other.ImageItem;
import com.treasure.hunt.strategy.Treasure;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.SearchPathPrototype;
import com.treasure.hunt.utils.JTSUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A turn contains every essential data, produced in each game turn.
 *
 * @author dorianreineccius
 */
@AllArgsConstructor
@Getter
public class Turn {
    /**
     * The {@link Hint} the {@link com.treasure.hunt.strategy.hider.Hider} gave.
     */
    private Hint hint;
    /**
     * The {@link SearchPathPrototype} the {@link com.treasure.hunt.strategy.searcher.Searcher} did.
     */
    private SearchPathPrototype searchPathPrototype;
    /**
     * The current location of the treasure.
     */
    private Treasure treasure;

    public Turn(Hint hint, SearchPathPrototype searchPathPrototype, Point treasure) {
        this.hint = hint;
        this.searchPathPrototype = searchPathPrototype;
        this.treasure = new Treasure(treasure);
    }

    /**
     * @return a list of all geometryItems of this.
     */
    public List<GeometryItem<?>> getGeometryItems() {
        List<GeometryItem<?>> output = new ArrayList<>();

        if (hint != null) {
            output.addAll(getHintGeometries());
        }

        if (searchPathPrototype != null) {
            output.addAll(getSearchPathGeometries());
        }

        if (treasure != null) {
            output.addAll(getTreasureGeometries());
        }

        output.sort(Comparator.comparingInt(a -> a.getGeometryStyle().getZIndex()));

        return output;
    }

    public void unselect() {
        if (hint != null) {
            hint.setSelected(false);
        }

        if (searchPathPrototype != null) {
            searchPathPrototype.setSelected(false);
        }

        if (treasure != null) {
            treasure.setSelected(false);
        }
    }

    /**
     * @return a list of all geometryItems of this.
     */
    public List<GeometryItem<?>> getSelectedGeometryItems() {
        List<GeometryItem<?>> output = new ArrayList<>();

        if (hint != null && hint.isSelected()) {
            output.addAll(getHintGeometries());
        }

        if (searchPathPrototype != null && searchPathPrototype.isSelected()) {
            output.addAll(getSearchPathGeometries());
        }

        if (treasure != null && treasure.isSelected()) {
            output.addAll(getTreasureGeometries());
        }

        output.sort(Comparator.comparingInt(a -> a.getGeometryStyle().getZIndex()));

        return output;
    }

    private List<GeometryItem<?>> getHintGeometries() {
        List<GeometryItem<?>> geometryItems = hint.getGeometryItems();
        geometryItems.addAll(hint.getAdditionalGeometryItems());
        return geometryItems;
    }

    private List<GeometryItem<?>> getTreasureGeometries() {
        return Arrays.asList(
                new GeometryItem<>(treasure.getPoint(), GeometryType.TREASURE),
                new GeometryItem<>(new ImageItem(treasure.getCoordinate(), 20, 20, "/images/target.png", ImageItem.Alignment.CENTER_CENTER), GeometryType.TREASURE_FLAG)
        );
    }

    private List<GeometryItem<?>> getSearchPathGeometries() {
        List<GeometryItem<?>> items = new ArrayList<>(searchPathPrototype.getPointList());

        items.addAll(searchPathPrototype.getLines());
        items.addAll(searchPathPrototype.getAdditional());

        if (searchPathPrototype.getFirstPoint() != null && searchPathPrototype.getLastPoint() != null) {
            items.add(new GeometryItem<>(
                    JTSUtils.createLineString(searchPathPrototype.getLastPoint(), searchPathPrototype.getFirstPoint()),
                    GeometryType.WAY_POINT
            ));

            items.add(new GeometryItem<>(new ImageItem(searchPathPrototype.getLastPoint().getCoordinate(), 20, 20, "/images/pin.png", ImageItem.Alignment.BOTTOM_CENTER), GeometryType.SEARCHER_LAST_MOVE));
        }

        return items;

    }


}
