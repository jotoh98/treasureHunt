package com.treasure.hunt.game;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import io.reactivex.annotations.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * A Move contains the essential elements of a move happened in the game.
 * It may consist of a {@link Hint}, a {@link Movement} and the current treasure location.
 *
 * @author dorianreineccius
 */
@AllArgsConstructor
@Getter
public class Move {
    @Nullable
    private Hint hint;
    @Nullable
    private Movement movement;
    @Nullable
    private Point treasureLocation;

    /**
     * @return a list of all geometryItems of this.
     */
    public List<GeometryItem> getGeometryItems() {
        List<GeometryItem> output = new ArrayList<>();
        if (hint != null) {
            output.addAll(hint.getGeometryItems());
            output.addAll(hint.getAdditionalGeometryItems());
        }
        if (movement != null) {
            output.addAll(movement.getPoints());
            output.addAll(movement.getAdditionalGeometryItems());
        }
        if (treasureLocation != null) {
            output.add(new GeometryItem(treasureLocation, GeometryType.TREASURE));
        }
        return output;
    }
}
