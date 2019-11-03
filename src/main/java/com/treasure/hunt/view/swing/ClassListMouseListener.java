package com.treasure.hunt.view.swing;

import com.treasure.hunt.utils.Reflections;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClassListMouseListener extends MouseAdapter {

    private ClassListCellRenderer renderer;
    private JList jList;
    private JList other;

    private int selectedIndex = -1;

    public ClassListMouseListener(JList jlist, JList other) {
        this.jList = jlist;
        this.other = other;
        renderer = (ClassListCellRenderer) jlist.getCellRenderer();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int clickedItemIndex = jList.locationToIndex(new Point(e.getX(), e.getY()));

        ClassListCellRenderer otherRenderer = (ClassListCellRenderer) other.getCellRenderer();

        Class generic = Reflections.interfaceGenericsClass((Class) jList.getModel().getElementAt(clickedItemIndex));

        otherRenderer.setOtherGeneric(generic);
        renderer.setOtherGeneric(generic);

        jList.setSelectedIndex(clickedItemIndex);

        super.mouseClicked(e);

        other.repaint();
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
