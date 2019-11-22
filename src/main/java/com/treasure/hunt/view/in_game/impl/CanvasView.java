package com.treasure.hunt.view.in_game.impl;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.jts.AdvancedShapeWriter;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.view.in_game.View;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CanvasView extends JPanel implements View {

    private AdvancedShapeWriter shapeWriter;

    @Getter
    private PointTransformation pointTransformation = new PointTransformation();

    private GameManager gameManager;
    private List<GeometryItem> geometryItems = new ArrayList<>();

    public CanvasView() {
        shapeWriter = new AdvancedShapeWriter(pointTransformation);
    }

    @Override
    public void run() {
        geometryItems = gameManager.getGeometryItems();
        revalidate();
        // This repaints the view, when new geometryItems appear.
        repaint();
    }

    private Shape draw(GeometryItem geometryItem) {
        return geometryItem.toShape(shapeWriter);
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

        if (!geometryItem.getGeometryStyle().isVisible()) {
            return;
        }

        Shape shape = draw(geometryItem);

        if (geometryItem.getGeometryStyle().isFilled()) {
            graphics2D.setColor(geometryItem.getGeometryStyle().getFillColor());
            graphics2D.fill(shape);
        }

        graphics2D.setColor(geometryItem.getGeometryStyle().getOutlineColor());
        graphics2D.draw(shape);
    }

    @Override
    public void init(GameManager gameManager) {
        this.gameManager = gameManager;
    }


    private Coordinate[] getBoundary() {
        return null;
    }
}
