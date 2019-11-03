package com.treasure.hunt.view.main;

import com.treasure.hunt.strategy.geom.GeometryItem;

import javax.swing.*;
import java.awt.*;

public class ItemEditorController extends JFrame {

    private GeometryItem geometryItem;

    private Box table = new Box(BoxLayout.Y_AXIS);

    private JTextField name = new JTextField("");
    private JCheckBox enabled = new JCheckBox();

    public ItemEditorController() {
        setSize(100, 200);
        setVisible(true);
        add(table);
    }

    public void setGeometryItem(GeometryItem geometryItem) {
        this.geometryItem = geometryItem;
        update();
    }

    private void update() {
        name.setText(geometryItem.getType().getDisplayName());
        name.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        addRow("Name", enabled);

        enabled.setSelected(geometryItem.getStyle().isVisible());
        addRow("Visible", enabled);
    }

    private void addRow(String name, JComponent comp) {
        Box row = new Box(BoxLayout.X_AXIS);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray));

        JLabel title = new JLabel(name);
        title.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.lightGray));
        row.add(title);

        row.add(comp);

        table.add(row);
    }

}
