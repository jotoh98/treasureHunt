package com.treasure.hunt.game;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This contains every essential data, produced in each move.
 *
 * @author dorianreineccius
 */
@AllArgsConstructor
@Getter
public class Move {
    /**
     * The {@link Hint} the {@link com.treasure.hunt.strategy.hider.Hider} gave.
     */
    private Hint hint;
    /**
     * The {@link Movement} the {@link com.treasure.hunt.strategy.searcher.Searcher} did.
     */
    private Movement movement;
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
            output.addAll(hint.getGeometryItems());
            output.addAll(hint.getAdditionalGeometryItems());
        }
        if (movement != null) {
            output.addAll(movement.getPoints());
            output.addAll(movement.getAdditionalGeometryItems());
        }
        if (treasureLocation != null) {
            output.add(new GeometryItem<>(treasureLocation, GeometryType.TREASURE));
        }
        output.sort(Comparator.comparingInt(a -> a.getGeometryStyle().getZIndex()));

        return output;
    }
}
