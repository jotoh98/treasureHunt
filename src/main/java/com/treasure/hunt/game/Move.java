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
 * This class contains all essential objects happened in a move.
 * The {@link Movement}, the {@link com.treasure.hunt.strategy.searcher.Searcher} did.
 * The {@link Hint}, the {@link com.treasure.hunt.strategy.hider.Hider} gave.
 * The current treasure location, specified by the {@link com.treasure.hunt.strategy.hider.Hider} given as {@link Point}.
 *
 * @author dorianreineccius
 */
@AllArgsConstructor
@Getter
public class Move {
    private Hint hint;
    private Movement movement;
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
