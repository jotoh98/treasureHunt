package com.treasure.hunt.view.swing;

import com.treasure.hunt.utils.Reflections;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClassListCellRenderer extends JPanel implements ListCellRenderer<Class> {

    @Getter
    @Setter
    int hoverIndex = -1;
    @Getter
    @Setter
    Class otherGeneric = null;
    private JLabel nameLabel = new JLabel();
    private JLabel genericLabel = new JLabel();

    @Override
    public Component getListCellRendererComponent(JList<? extends Class> list, Class value, int index, boolean isSelected, boolean cellHasFocus) {
        boolean isHovered = index == hoverIndex;
        boolean isAvailable = Reflections.interfaceGenericsClass(value) == otherGeneric;

        if (otherGeneric == null)
            isAvailable = true;

        if (isSelected && !isAvailable) {
            list.clearSelection();
        }

        list.setOpaque(false);
        setFont(list.getFont());
        setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        nameLabel.setBorder(new EmptyBorder(new Insets(0, 0, 4, 0)));

        setBackground(new Color(70, 70, 70, isSelected ? 255 : 0));

        if (isHovered)
            if (isSelected)
                setBackground(new Color(23, 53, 187));
            else
                setBackground(new Color(24, 53, 136));
        else if (isSelected)
            setBackground(new Color(70, 70, 70));
        else
            setBackground(new Color(255, 255, 255, 0));


        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        nameLabel.setText(value.getSimpleName());

        if (isAvailable)
            nameLabel.setForeground(Color.white);
        else
            nameLabel.setForeground(new Color(255, 255, 255, 88));

        add(nameLabel);

        genericLabel.setText(String.format("(%s)", Reflections.genericName(value)));

        if (isAvailable)
            genericLabel.setForeground(Color.gray);
        else
            genericLabel.setForeground(new Color(128, 128, 128, 88));

        add(genericLabel);

        return this;
    }
}
