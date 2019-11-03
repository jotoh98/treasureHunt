package com.treasure.hunt.view.main;

import com.treasure.hunt.SwingTest;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.view.swing.ClassListCellRenderer;
import com.treasure.hunt.view.swing.ClassListMouseListener;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

@Slf4j
public class MainMenuController {
    private final JFrame jFrame = new JFrame("Treasure Hunt");
    private final JPanel rootPanel = new JPanel();
    private JPanel selectStrategyContainer = new JPanel();
    private JPanel selectContextContainer = new JPanel();
    private JButton playButton = new JButton("Play");
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

        listBehaviour();

        fillLists();
    }

    private void listBehaviour() {

        searcherListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hiderListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        searcherListView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hiderListView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ClassListMouseListener searcherListListener = new ClassListMouseListener(searcherListView, hiderListView);
        searcherListView.addMouseListener(searcherListListener);
        searcherListView.addMouseMotionListener(searcherListListener);

        ClassListMouseListener hiderListListener = new ClassListMouseListener(hiderListView, searcherListView);
        hiderListView.addMouseListener(hiderListListener);
        hiderListView.addMouseMotionListener(hiderListListener);

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
        errorLabel.setVisible(false);
        GeometryItem[] items = SwingTest.exampleGeometryItems();
        CanvasController canvasController = new CanvasController();
        canvasController.setGeometryItems(items);

        ItemEditorController itemEditorController = new ItemEditorController();
        itemEditorController.setGeometryItem(items[3]);

        canvasController.setVisible(true);
    }

    private void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.X_AXIS));
        rootPanel.setBackground(new Color(46, 48, 50));
        rootPanel.add(selectStrategyContainer);
        rootPanel.add(selectContextContainer);

        selectStrategyContainer.setAlignmentY(Component.TOP_ALIGNMENT);
        selectStrategyContainer.setLayout(new BoxLayout(selectStrategyContainer, BoxLayout.X_AXIS));
        selectStrategyContainer.setOpaque(false);
        selectStrategyContainer.add(hiderListView, Component.TOP_ALIGNMENT);
        selectStrategyContainer.add(searcherListView, Component.TOP_ALIGNMENT);

        hiderListView.setAlignmentY(Component.TOP_ALIGNMENT);
        hiderListView.setCellRenderer(new ClassListCellRenderer());

        searcherListView.setAlignmentY(Component.TOP_ALIGNMENT);
        searcherListView.setCellRenderer(new ClassListCellRenderer());

        selectContextContainer.setAlignmentY(Component.TOP_ALIGNMENT);
        selectContextContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        selectContextContainer.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(78, 78, 78)));
        selectContextContainer.setLayout(new BoxLayout(selectContextContainer, BoxLayout.Y_AXIS));
        selectContextContainer.setBackground(new Color(0x35373A));
        selectContextContainer.add(playButton);
        selectContextContainer.add(errorLabel);

        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setVisible(false);
        errorLabel.setForeground(Color.RED);

        jFrame.setContentPane(rootPanel);
        jFrame.setResizable(false);
        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.setSize(new Dimension(600, 400));

    }
}
