package com.treasure.hunt.view.in_game.implementatons;

import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.view.in_game.View;
import lombok.RequiredArgsConstructor;

import java.awt.*;

@RequiredArgsConstructor
public class CanvasView implements View {
    private final Canvas canvas;


    @Override
    public void visualizeProduct(Product product) {

    }

    protected void drawItem(GeometryItem geometryItem) {

    }
}
