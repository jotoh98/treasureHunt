package com.treasure.hunt.view.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class ClassListMouseListener extends ClassListHoverListener {
    private final List<JList> toBeRePaintedOnSelection;

    public ClassListMouseListener(JList jlist, List<JList> toBeRePaintedOnSelection) {
        super(jlist);
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
    public void mouseDragged(MouseEvent e) {
        mouseClicked(e);
    }
}
