package com.treasure.hunt.view.swing;

import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.AllArgsConstructor;
import org.locationtech.jts.awt.IdentityPointTransformation;
import org.locationtech.jts.awt.PointTransformation;
import org.locationtech.jts.awt.ShapeWriter;

import javax.swing.*;
import java.awt.*;

@AllArgsConstructor
public class GeometryPanel extends JPanel {

    private PointTransformation pointTransformation;

    GeometryPanel() {
        pointTransformation = new IdentityPointTransformation();
    }

    public void paint(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;

        RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2D.addRenderingHints(renderingHints);

        drawGeometryItems(graphics2D, SwingTest.exampleGeometryItems());
    }


    private void drawGeometryItems(Graphics2D graphics2D, GeometryItem[] geometryItems) {
        for (GeometryItem geometryItem : geometryItems)
            drawGeometryItem(graphics2D, geometryItem);
    }

    private void drawGeometryItem(Graphics2D graphics2D, GeometryItem geometryItem) {
        if (!geometryItem.getType().isVisibleByDefault())
            return;

        ShapeWriter shapeWriter = new ShapeWriter(pointTransformation);


        Shape shape = shapeWriter.toShape(geometryItem.getObject());

        if (geometryItem.getType().isFilled()) {
            graphics2D.setColor(geometryItem.getType().getFillColor());
            graphics2D.fill(shape);
        }

        graphics2D.setColor(geometryItem.getType().getLineColor());
        graphics2D.draw(shape);
    }

}
