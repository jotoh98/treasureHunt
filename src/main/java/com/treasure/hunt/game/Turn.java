package com.treasure.hunt.game;

import com.treasure.hunt.jts.other.ImageItem;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.SearchPath;
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
     * The {@link SearchPath} the {@link com.treasure.hunt.strategy.searcher.Searcher} did.
     */
    private SearchPath searchPath;
    /**
     * The current location of the treasure.
     */
    private Point treasureLocation;

    /**
     * @return a list of all geometryItems of this.
     */
    public List<GeometryItem<?>> getGeometryItems() {
        List<GeometryItem<?>> output = new ArrayList<>();

        if (hint != null) {
            output.addAll(getHintGeometries());
        }

        if (searchPath != null) {
            output.addAll(getSearchPathGeometries());
        }

        if (treasureLocation != null) {
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
                new GeometryItem<>(treasureLocation, GeometryType.TREASURE),
                new GeometryItem<>(new ImageItem(treasureLocation.getCoordinate(), 20, 20, "/images/icon/target.png", ImageItem.Alignment.CENTER_CENTER), GeometryType.TREASURE_FLAG)
        );
    }

    private List<GeometryItem<?>> getSearchPathGeometries() {
        List<GeometryItem<?>> items = new ArrayList<>(searchPath.getPointsExceptTheFirst());
        items.addAll(searchPath.getLineGeometryItems());
        items.addAll(searchPath.getAdditional());

        items.add(new GeometryItem<>(new ImageItem(searchPath.getLastPoint().getCoordinate(), 20, 20, "/images/icon/pin.png", ImageItem.Alignment.BOTTOM_CENTER), GeometryType.CURRENT_WAY_POINT));

        return items;

    }


}
