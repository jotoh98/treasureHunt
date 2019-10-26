package com.treasure.hunt.ui.swing;

import com.treasure.hunt.jts.Circle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.awt.PointTransformation;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.math.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

@AllArgsConstructor
public class GeometryCanvas extends JComponent {

    @Getter
    private Vector2D offset = new Vector2D();
    @Getter
    private double scale = 1.0;
    PointTransformation pointTransformation = new PointTransformation() {
        @Override
        public void transform(Coordinate src, Point2D dest) {
            dest.setLocation(scale * src.x + offset.getX(), scale * src.y + offset.getY());
        }
    };

    GeometryCanvas() {
        GeometryCanvasMouseListener geometryCanvasMouseListener = new GeometryCanvasMouseListener(this);
        addMouseMotionListener(geometryCanvasMouseListener);
        addMouseListener(geometryCanvasMouseListener);
        addMouseWheelListener(geometryCanvasMouseListener);
    }

    public void setOffset(Vector2D offset) {
        this.offset = offset;
    }

    public void setScale(double scale) {
        this.scale = Math.max(0.00001, Math.min(10.0, scale));
    }

    public void paint(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;

        RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2D.addRenderingHints(renderingHints);

        new GeometryItem<>(new Circle(new Coordinate(0, 0), 10.0, new GeometryFactory()));
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
