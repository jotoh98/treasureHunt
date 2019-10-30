package com.treasure.hunt.view.in_game.implementatons;

import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.view.in_game.View;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.awt.ShapeWriter;

import javax.swing.*;
import java.awt.*;

public class CanvasView extends JPanel implements View<Shape> {

    @Getter
    private ShapeWriter shapeWriter;

    @Getter
    private PointTransformation pointTransformation = new PointTransformation();

    @Setter
    private GeometryItem[] geometryItems = new GeometryItem[0];

    @Override
    public void run() {

    }

    public CanvasView() {
        RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        shapeWriter = new ShapeWriter(pointTransformation);
    }

    @Override
    public Shape transfer(GeometryItem geometryItem) {
        return shapeWriter.toShape(geometryItem.getObject());
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;

        for (GeometryItem geometryItem : geometryItems)
            paintShape(graphics2D, geometryItem);
    }

    public void paintShape(Graphics2D graphics2D, GeometryItem geometryItem) {

        if (!geometryItem.getStyle().isVisible())
            return;

        Shape shape = transfer(geometryItem);

        if (geometryItem.getStyle().isFilled()) {
            graphics2D.setColor(geometryItem.getStyle().getFillColor());
            graphics2D.fill(shape);
        }

        graphics2D.setColor(geometryItem.getStyle().getOutlineColor());
        graphics2D.draw(shape);
    }
}
