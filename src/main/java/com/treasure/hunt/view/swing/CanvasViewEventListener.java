package com.treasure.hunt.view.swing;

import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.view.in_game.impl.CanvasView;
import com.treasure.hunt.view.main.CanvasController;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.event.*;

@RequiredArgsConstructor
@Slf4j
public class CanvasViewEventListener implements MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener {

    private static final double MIN_SCALE = .1;
    private static final double MAX_SCALE = 1e10;
    private Vector2D dragOffset;
    @NonNull
    private CanvasController canvasController;

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dragOffset = new Vector2D(e.getX(), e.getY()).subtract(canvasController.getCanvasView().getPointTransformation().getOffset());
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
        canvasController.getCanvasView().getPointTransformation().setOffset(dragDelta);
        canvasController.getCanvasView().repaint();
        canvasController.updateInspectors();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() == 0) {
            return;
        }

        Vector2D mouse = new Vector2D(e.getX(), e.getY());

        CanvasView canvasView = canvasController.getCanvasView();
        PointTransformation transformer = canvasView.getPointTransformation();
        Vector2D direction = transformer.getOffset().subtract(mouse);

        double oldScale = transformer.getScale();
        double scrollAmount = Math.pow(1.1, e.getScrollAmount());

        if (e.getWheelRotation() < 0) {
            scrollAmount = 1 / scrollAmount;
        }

        log.info(String.format("%s %s", e.getWheelRotation(), e.getScrollAmount()));

        double newScale = oldScale * scrollAmount;

        if (newScale > 0) {
            transformer.setScale(newScale);
            transformer.setOffset(mouse.add(direction.multiply(newScale / oldScale)));
            canvasController.updateInspectors();
            canvasController.updateScalePanel(newScale);
            canvasView.repaint();
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        CanvasView canvasView = canvasController.getCanvasView();
        int width = canvasView.getWidth();
        int height = canvasView.getHeight();
        canvasView.getPointTransformation().updateCanvasSize(width, height);
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
