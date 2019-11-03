package com.treasure.hunt.view.in_game.implementatons;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.view.in_game.View;

public class EmptyView implements View<Object> {

    @Override
    public Object transfer(GeometryItem geometryItem) {
        return null;
    }

    @Override
    public void run() {

    }
}
