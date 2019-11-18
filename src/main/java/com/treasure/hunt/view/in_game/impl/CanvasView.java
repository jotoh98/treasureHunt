package com.treasure.hunt.view.in_game.impl;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.view.in_game.View;
import lombok.Getter;
import org.locationtech.jts.awt.ShapeWriter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CanvasView extends JPanel implements View {

    private ShapeWriter shapeWriter;

    @Getter
    private PointTransformation pointTransformation = new PointTransformation();

    private GameHistory gameHistory;
    private List<GeometryItem> geometryItems = new ArrayList<>();

    public CanvasView() {
        shapeWriter = new ShapeWriter(pointTransformation);
    }

    @Override
    public void run() {
        geometryItems = gameHistory.getGeometryItems();
        revalidate();
    }

    public Shape draw(GeometryItem geometryItem) {
        return shapeWriter.toShape(geometryItem.getObject());
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;

        for (GeometryItem geometryItem : geometryItems) {
            paintShape(graphics2D, geometryItem);
        }

    }

    public void paintShape(Graphics2D graphics2D, GeometryItem geometryItem) {

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
    public void init(GameHistory gameHistory) {
        this.gameHistory = gameHistory;
    }
}
