package com.treasure.hunt.view.in_game.impl;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.jts.AdvancedShapeWriter;
import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.view.in_game.View;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CanvasView extends JPanel implements View {

    @Getter
    private AdvancedShapeWriter shapeWriter;

    @Getter
    private PointTransformation pointTransformation = new PointTransformation();

    private GameHistory gameHistory;
    private List<GeometryItem> geometryItems = new ArrayList<>();

    public CanvasView() {
        shapeWriter = new AdvancedShapeWriter(pointTransformation);
    }

    @Override
    public void run() {
        geometryItems = gameHistory.getGeometryItems();
        revalidate();
        // This repaints the view, when new geometryItems appear.
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;

        for (GeometryItem geometryItem : geometryItems) {
            paintShape(graphics2D, geometryItem);
        }

    }

    private void paintShape(Graphics2D graphics2D, GeometryItem geometryItem) {
        geometryItem.draw(graphics2D, shapeWriter);
    }

    @Override
    public void init(GameHistory gameHistory) {
        this.gameHistory = gameHistory;
    }
}
