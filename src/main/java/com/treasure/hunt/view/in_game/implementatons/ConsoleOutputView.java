package com.treasure.hunt.view.in_game.implementatons;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.view.in_game.View;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ConsoleOutputView implements View {

    private GameHistory gameHistory;
    private List<GeometryItem> lastItems;

    public void draw(GeometryItem geometryItem) {
        //product.getGeometryItems().forEach(geometryItem -> log.info(geometryItem.getObject().toString()));
        log.info(geometryItem.toString());
    }

    @Override
    public void run() {
        List<GeometryItem> geometryItems = gameHistory.getGeometryItems();
        List<GeometryItem> filteredGeometryItems = new ArrayList<>(geometryItems);
        if (lastItems != null) {
            filteredGeometryItems.removeAll(lastItems);
        }
        filteredGeometryItems.forEach(this::draw);
        lastItems = geometryItems;
    }

    @Override
    public void init(GameHistory gameHistory) {
        this.gameHistory = gameHistory;
    }
}
