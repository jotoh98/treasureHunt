package com.treasure.hunt.view.main;

import com.treasure.hunt.strategy.seeker.Seeker;
import com.treasure.hunt.strategy.tipster.Tipster;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

@Slf4j
public class MainMenuController {
    private final JFrame jFrame = new JFrame("Main Menu");
    private final JPanel rootPanel = new JPanel();
    private JPanel selectStrategyContainer = new JPanel();
    private JButton playButton;
    private JLabel errorLabel = new JLabel();
    private DefaultListModel<Class<? extends Tipster>> tipsterList = new DefaultListModel<>();
    private final JList<Class<? extends Tipster>> tipsterListView = new JList<>(tipsterList);
    private DefaultListModel<Class<? extends Seeker>> seekerList = new DefaultListModel<>();
    private final JList<Class<? extends Seeker>> seekerListView = new JList<>(seekerList);

    public void show() {
        init();

        playButton.addActionListener(e -> {
            startGame();
        });

        fillLists();
    }

    private void fillLists() {
        Reflections seekerReflections = new Reflections("com.treasure.hunt.strategy.seeker.implementations");
        Reflections tipsterReflections = new Reflections("com.treasure.hunt.strategy.tipster.implementations");

        Set<Class<? extends Seeker>> allSeekers = seekerReflections.getSubTypesOf(Seeker.class);
        allSeekers.forEach(aClass -> seekerList.addElement(aClass));

        Set<Class<? extends Tipster>> allTipsters = tipsterReflections.getSubTypesOf(Tipster.class);
        allTipsters.forEach(aClass -> tipsterList.addElement(aClass));
    }

    private void startGame() {
        Class<? extends Seeker> selectedSeeker = seekerListView.getSelectedValue();
        Class<? extends Tipster> selectedTipster = tipsterListView.getSelectedValue();
        if (selectedSeeker == null || selectedTipster == null) {
            errorLabel.setVisible(true);
            errorLabel.setText("Please select both a seeker and a tipster.");
            return;
        }
        Type actualTypeArgumentSeeker = ((ParameterizedType) (selectedSeeker.getGenericInterfaces()[0])).getActualTypeArguments()[0];
        Type actualTypeArgumentTipster = ((ParameterizedType) (selectedTipster.getGenericInterfaces()[0])).getActualTypeArguments()[0];
        if (!actualTypeArgumentSeeker.equals(actualTypeArgumentTipster)) {
            errorLabel.setVisible(true);
            errorLabel.setText("Please select a seeker and a tipster that are compatible.");
            return;
        }
        log.info("We did it");
    }

    private void init() {
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        selectStrategyContainer.setLayout(new BoxLayout(selectStrategyContainer, BoxLayout.X_AXIS));
        rootPanel.add(selectStrategyContainer);
        playButton = new JButton("Play");
        seekerListView.setBorder(new EmptyBorder(10, 10, 10, 10));
        tipsterListView.setBorder(new EmptyBorder(10, 10, 10, 10));
        Component verticalStrut = Box.createVerticalStrut(0);
        verticalStrut.setMaximumSize(new Dimension(0, Integer.MAX_VALUE));
        rootPanel.add(verticalStrut);
        rootPanel.add(playButton);
        rootPanel.add(errorLabel, BorderLayout.CENTER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setVisible(false);
        errorLabel.setForeground(Color.RED);
        selectStrategyContainer.add(tipsterListView);
        selectStrategyContainer.add(seekerListView);
        jFrame.setContentPane(rootPanel);
        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.setSize(new Dimension(800, 800));

        DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Class item = (Class) value;
                String name = item.getSimpleName();
                Type genericType = ((ParameterizedType) (item.getGenericInterfaces()[0])).getActualTypeArguments()[0];
                String format = null;
                try {
                    format = String.format("%s (%s)", name, Class.forName(((Class) genericType).getName()).getSimpleName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return super.getListCellRendererComponent(list, format, index, isSelected, cellHasFocus);
            }
        };
        seekerListView.setCellRenderer(cellRenderer);
        tipsterListView.setCellRenderer(cellRenderer);
    }
}
