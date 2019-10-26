package com.treasure.hunt.ui.swing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.event.*;

@RequiredArgsConstructor
public class GeometryCanvasMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {

    private Vector2D dragOffset = null;

    private double scale = 1.0;

    @NonNull
    private GeometryCanvas geometryCanvas;

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        geometryCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        geometryCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        dragOffset = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragOffset == null)
            dragOffset = new Vector2D(e.getX(), e.getY()).subtract(geometryCanvas.getOffset());

        Vector2D dragDelta = new Vector2D(e.getX(), e.getY()).subtract(dragOffset);
        geometryCanvas.setOffset(dragDelta);
        geometryCanvas.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Vector2D mouse = new Vector2D(e.getX(), e.getY());
        Vector2D direction = geometryCanvas.getOffset().subtract(mouse);

        double old_scale = scale;
        double scrollDelta = -e.getPreciseWheelRotation() * 0.1;
        setScale(scale + scrollDelta);
        geometryCanvas.setScale(scale);

        geometryCanvas.setOffset(mouse.add(direction.multiply(scale / old_scale)));

        geometryCanvas.repaint();
    }

    protected void setScale(double scale) {
        this.scale = Math.max(0.01, Math.min(10.0, scale));
    }
}
