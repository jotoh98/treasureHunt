package com.treasure.hunt.view.swing;

import com.treasure.hunt.view.main.CanvasController;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.event.*;

/**
 * @author axel12, hassel
 */
public class CanvasMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {

    private static final double MIN_SCALE = .1;
    private static final double MAX_SCALE = 1e2;
    private Vector2D dragOffset;
    private CanvasController canvasController;

    public CanvasMouseListener(CanvasController canvasController) {
        this.canvasController = canvasController;
    }

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

        double oldScale = canvasController.getScale();
        double scrollDelta = -e.getPreciseWheelRotation() * 1e-2;
        double newScale = oldScale + scrollDelta;

        if (newScale >= MIN_SCALE && newScale <= MAX_SCALE) {
            canvasController.addToScale(scrollDelta);
            canvasController.setOffset(mouse.add(direction.multiply(newScale / oldScale)));
            canvasController.getCanvasView().repaint();
        }
    }
}
