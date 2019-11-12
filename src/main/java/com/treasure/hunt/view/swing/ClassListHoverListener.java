package com.treasure.hunt.view.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClassListHoverListener extends MouseAdapter {

    final JList jList;
    ClassListCellRenderer renderer;


    public ClassListHoverListener(JList jlist) {
        this.jList = jlist;
        renderer = (ClassListCellRenderer) jlist.getCellRenderer();
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
}
