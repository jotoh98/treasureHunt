package com.treasure.hunt.game.mods.obstacles;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface ObstacleHider extends Hider {

    /**
     * Use this to initialize your hider.
     *
     * @param treasurePosition The initial treasure position
     * @param obstacles the obstacles, placed in the game
     * @param gameHistory The {@link GameHistory}, the hider dumps its {@link com.treasure.hunt.strategy.Product}'s
     *                    by {@link Hider#commitProduct(Product)}
     */
    void init(Point treasurePosition, List<GeometryItem> obstacles, GameHistory gameHistory);

}
