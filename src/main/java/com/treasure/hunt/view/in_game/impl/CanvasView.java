package com.treasure.hunt.view.in_game.impl;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.view.in_game.View;
import org.locationtech.jts.awt.ShapeWriter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This starts a concurrent Thread containing a Canvas,
 * on which the GameSimulation will be illustrated.
 *
 * @author axel12, hassel
 */
public class CanvasView extends JPanel implements View {

    private ShapeWriter shapeWriter;

    private PointTransformation pointTransformation = new PointTransformation();

    private GameManager gameManager;
    private List<GeometryItem> geometryItems = new ArrayList<>();

    public CanvasView() {
        shapeWriter = new ShapeWriter(pointTransformation);
    }

    /**
     * This repaints the canvas with the current state of the {@link GameManager}.
     */
    @Override
    public void run() {
        geometryItems = gameManager.getGeometryItems();
        repaint();
    }

    /**
     * @param graphics where we paint all the new picture.
     */
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics;

        for (GeometryItem geometryItem : geometryItems) {
            paintShape(graphics2D, geometryItem);
        }

    }

    /**
     * @param gameManager the {@link GameManager}, the View gets its {@link com.treasure.hunt.game.Move} objects.
     */
    @Override
    public void init(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * @param graphics2D   we draw the geometryItem on
     * @param geometryItem the geometryItem, we want to draw on the graphics2D
     */
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

    /**
     * @param geometryItem to convert to a {@link Shape}.
     * @return the geometryItem converted to a {@link Shape}.
     */
    private Shape draw(GeometryItem geometryItem) {
        return shapeWriter.toShape(geometryItem.getObject());
    }

    public PointTransformation getPointTransformation() {
        return this.pointTransformation;
    }
}
