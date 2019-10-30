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
        int i = jList.locationToIndex(new Point(e.getX(), e.getY()));

        ClassListCellRenderer otherRenderer = (ClassListCellRenderer) other.getCellRenderer();

        Class generic = Reflections.interfaceGenericsClass((Class) jList.getModel().getElementAt(i));

        otherRenderer.setOtherGeneric(generic);
        renderer.setOtherGeneric(generic);

        jList.setSelectedIndex(i);

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

        int i = jList.locationToIndex(new Point(e.getX(), e.getY()));

        if (i != renderer.getHoverIndex()) {
            renderer.setHoverIndex(i);
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
