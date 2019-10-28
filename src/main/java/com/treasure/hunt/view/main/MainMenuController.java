package com.treasure.hunt.view.main;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
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
    private DefaultListModel<Class<? extends Hider>> hiderList = new DefaultListModel<>();
    private final JList<Class<? extends Hider>> hiderListView = new JList<>(hiderList);
    private DefaultListModel<Class<? extends Searcher>> searcherList = new DefaultListModel<>();
    private final JList<Class<? extends Searcher>> searcherListView = new JList<>(searcherList);

    public void show() {
        init();

        playButton.addActionListener(e -> {
            startGame();
        });

        fillLists();
    }

    private void fillLists() {
        Reflections searcherReflections = new Reflections("com.treasure.hunt.strategy.searcher.implementations");
        Reflections hiderReflections = new Reflections("com.treasure.hunt.strategy.hider.implementations");

        Set<Class<? extends Searcher>> allSearchers = searcherReflections.getSubTypesOf(Searcher.class);
        allSearchers.forEach(aClass -> searcherList.addElement(aClass));

        Set<Class<? extends Hider>> allHiders = hiderReflections.getSubTypesOf(Hider.class);
        allHiders.forEach(aClass -> hiderList.addElement(aClass));
    }

    private void startGame() {
        Class<? extends Searcher> selectedSearcher = searcherListView.getSelectedValue();
        Class<? extends Hider> selectedHider = hiderListView.getSelectedValue();
        if (selectedSearcher == null || selectedHider == null) {
            errorLabel.setVisible(true);
            errorLabel.setText("Please select both a searcher and a hider.");
            return;
        }
        Type actualTypeArgumentSearcher = ((ParameterizedType) (selectedSearcher.getGenericInterfaces()[0])).getActualTypeArguments()[0];
        Type actualTypeArgumentHider = ((ParameterizedType) (selectedHider.getGenericInterfaces()[0])).getActualTypeArguments()[0];
        if (!actualTypeArgumentSearcher.equals(actualTypeArgumentHider)) {
            errorLabel.setVisible(true);
            errorLabel.setText("Please select a searcher and a hider that are compatible.");
            return;
        }
        log.info("We did it");
    }

    private void init() {
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        selectStrategyContainer.setLayout(new BoxLayout(selectStrategyContainer, BoxLayout.X_AXIS));
        rootPanel.add(selectStrategyContainer);
        playButton = new JButton("Play");
        searcherListView.setBorder(new EmptyBorder(10, 10, 10, 10));
        hiderListView.setBorder(new EmptyBorder(10, 10, 10, 10));
        Component verticalStrut = Box.createVerticalStrut(0);
        verticalStrut.setMaximumSize(new Dimension(0, Integer.MAX_VALUE));
        rootPanel.add(verticalStrut);
        rootPanel.add(playButton);
        rootPanel.add(errorLabel, BorderLayout.CENTER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setVisible(false);
        errorLabel.setForeground(Color.RED);
        selectStrategyContainer.add(hiderListView);
        selectStrategyContainer.add(searcherListView);
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
        searcherListView.setCellRenderer(cellRenderer);
        hiderListView.setCellRenderer(cellRenderer);
    }
}
