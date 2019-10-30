package com.treasure.hunt.view.swing;

import com.treasure.hunt.view.main.CanvasController;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.event.*;

@RequiredArgsConstructor
public class CanvasMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {

    private Vector2D dragOffset;

    @NonNull
    private CanvasController canvasController;

    private double min_scale = .1;

    private double max_scale = 1e2;


    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dragOffset = new Vector2D(e.getX(), e.getY()).subtract(canvasController.getOffset());
        canvasController.getCanvasView().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        canvasController.getCanvasView().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Vector2D dragDelta = new Vector2D(e.getX(), e.getY()).subtract(dragOffset);
        canvasController.setOffset(dragDelta);
        canvasController.getCanvasView().repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Vector2D mouse = new Vector2D(e.getX(), e.getY());
        Vector2D direction = canvasController.getOffset().subtract(mouse);

        double old_scale = canvasController.getScale();
        double scrollDelta = -e.getPreciseWheelRotation() * 1e-2;
        double new_scale = old_scale + scrollDelta;


        if (new_scale >= min_scale && new_scale <= max_scale) {
            canvasController.addToScale(scrollDelta);
            canvasController.setOffset(mouse.add(direction.multiply(new_scale / old_scale)));
            canvasController.getCanvasView().repaint();
        }
    }
}
