package com.treasure.hunt.view.swing;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import javax.swing.*;

public class PointInspector extends JPanel {

    private JTextPane xTextPane = new JTextPane();
    private JTextPane yTextPane = new JTextPane();
    private JPanel xRowPanel = new JPanel();
    private JPanel yRowPanel = new JPanel();

    public PointInspector(double x, double y) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(xRowPanel);
        add(yRowPanel);

        xRowPanel.setLayout(new BoxLayout(xRowPanel, BoxLayout.X_AXIS));
        yRowPanel.setLayout(new BoxLayout(yRowPanel, BoxLayout.X_AXIS));

        xRowPanel.setSize(100, 30);
        yRowPanel.setSize(100, 30);

        setValue(x, y);

        JLabel comp = new JLabel("x: ");
        xRowPanel.add(comp);
        xRowPanel.add(xTextPane);

        JLabel comp1 = new JLabel("y: ");
        yRowPanel.add(comp1);
        yRowPanel.add(yTextPane);
    }

    public PointInspector() {
        this(0, 0);
    }


    public void setValue(double x, double y) {
        xTextPane.setText(String.valueOf(x));
        yTextPane.setText(String.valueOf(y));
    }

    public void setValue(Coordinate point) {
        setValue(point.x, point.y);
    }

    public void setValue(Vector2D vector2D) {
        setValue(vector2D.getX(), vector2D.getY());
    }
}
