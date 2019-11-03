package com.treasure.hunt.view.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ClassListMouseListener extends MouseAdapter {

    private ClassListCellRenderer renderer;
    private final JList jList;
    private final List<JList> toBeRePaintedOnSelection;


    public ClassListMouseListener(JList jlist, List<JList> toBeRePaintedOnSelection) {
        this.jList = jlist;
        renderer = (ClassListCellRenderer) jlist.getCellRenderer();
        this.toBeRePaintedOnSelection = toBeRePaintedOnSelection;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int clickedItemIndex = jList.locationToIndex(new Point(e.getX(), e.getY()));

        jList.setSelectedIndex(clickedItemIndex);
        super.mouseClicked(e);
        //The following should not be done in the asynchronously called paint method
        for (JList list : toBeRePaintedOnSelection) {
            Class selectedValue = (Class) list.getSelectedValue();
            if (selectedValue == null) {
                list.repaint();
                break;
            }
            Boolean available = ((ClassListCellRenderer) list.getCellRenderer()).isAvailableFunction.apply(selectedValue);
            if (!available) {
                list.clearSelection();
            }
            list.repaint();
        }

        jList.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int hoveredItemIndex = jList.locationToIndex(new Point(e.getX(), e.getY()));

        if (hoveredItemIndex != renderer.getHoverIndex()) {
            renderer.setHoverIndex(hoveredItemIndex);
            jList.repaint();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        renderer.setHoverIndex(-1);
        jList.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseClicked(e);
    }
}
