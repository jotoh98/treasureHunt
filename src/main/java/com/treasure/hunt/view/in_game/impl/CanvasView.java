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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the awt {@link Shape} view displaying the rendered coordinate system. Being a {@link View} instance, it
 * also reacts and repaints every time a new {@link GeometryItem} is sent to the {@link GameHistory}.
 *
 * @version 1.0
 */
public class CanvasView extends JPanel implements View {

    @Getter
    private AdvancedShapeWriter shapeWriter;

    @Getter
    private PointTransformation pointTransformation = new PointTransformation();

    private GameHistory gameHistory;
    private List<GeometryItem> geometryItems = new ArrayList<>();

    private List<GeometryItem> additionalItems = new ArrayList<>();

    public CanvasView() {
        shapeWriter = new AdvancedShapeWriter(pointTransformation);
    }

    public void addGeometryItem(GeometryItem item) {
        additionalItems.add(item);
    }

    @Override
    public void run() {
        geometryItems = Stream
                .of(gameHistory.getGeometryItems(), additionalItems)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        revalidate();
        // This repaints the view, when new geometryItems appear.
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;

        //TODO: sort geometryItems by z-index
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
