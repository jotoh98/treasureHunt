package com.treasure.hunt.view.main;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.view.in_game.implementatons.CanvasView;
import com.treasure.hunt.view.swing.CanvasMouseListener;
import lombok.Getter;
import org.locationtech.jts.math.Vector2D;

import javax.swing.*;

public class CanvasController extends JFrame {

    @Getter
    CanvasView canvasView;

    @Getter
    private Vector2D offset = new Vector2D();

    @Getter
    private double scale = 1.0;

    CanvasController(CanvasView canvasView) {
        this.canvasView = canvasView;
        CanvasMouseListener canvasMouseListener = new CanvasMouseListener(this);
        canvasView.addMouseMotionListener(canvasMouseListener);
        canvasView.addMouseListener(canvasMouseListener);
        canvasView.addMouseWheelListener(canvasMouseListener);
        setVisible(true);
        setSize(1080, 600);
        add(canvasView);
    }

    void setGeometryItems(GeometryItem[] geometryItems) {
        this.canvasView.setGeometryItems(geometryItems);
    }

    public void setOffset(Vector2D vector2D) {
        offset = vector2D;
        canvasView.getPointTransformation().setOffset(offset);
    }

    public void addToScale(double addend) {
        this.scale += addend;
        this.getCanvasView().getPointTransformation().setScale(this.scale);
    }
}
