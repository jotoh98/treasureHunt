package com.treasure.hunt.view.swing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Function;

@RequiredArgsConstructor
public class ClassListCellRenderer extends JPanel implements ListCellRenderer<Class> {

    public final Function<Class, Boolean> isAvailableFunction;
    private final Function<Class, String> getSubTitle;
    @Getter
    @Setter
    int hoverIndex = -1;
    private JLabel nameLabel = new JLabel();
    private JLabel subTitleLabel = new JLabel();

    @Override
    public Component getListCellRendererComponent(JList<? extends Class> list, Class value, int index, boolean isSelected, boolean cellHasFocus) {
        boolean isHovered = index == hoverIndex;
        boolean isAvailable = isAvailableFunction.apply(value);

        list.setOpaque(false);
        setFont(list.getFont());
        setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        nameLabel.setBorder(new EmptyBorder(new Insets(0, 0, 4, 0)));

        setBackground(new Color(70, 70, 70, isSelected ? 255 : 0));

        if (isHovered) {
            if (isSelected) {
                setBackground(new Color(23, 53, 187));
            } else {
                setBackground(new Color(24, 53, 136));
            }
        } else if (isSelected) {
            setBackground(new Color(70, 70, 70));
        } else {
            setBackground(new Color(255, 255, 255, 0));
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        nameLabel.setText(value.getSimpleName());

        if (isAvailable) {
            nameLabel.setForeground(Color.white);
        } else {
            nameLabel.setForeground(new Color(255, 255, 255, 88));
        }

        add(nameLabel);

        subTitleLabel.setText(String.format("(%s)", getSubTitle.apply(value)));

        if (isAvailable) {
            subTitleLabel.setForeground(Color.gray);
        } else {
            subTitleLabel.setForeground(new Color(128, 128, 128, 88));
        }

        add(subTitleLabel);

        return this;
    }
}
