package com.treasure.hunt.view.main;

import com.treasure.hunt.SwingTest;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.seeker.Seeker;
import com.treasure.hunt.strategy.tipster.Tipster;
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
    private DefaultListModel<Class<? extends Tipster>> tipsterList = new DefaultListModel<>();
    private final JList<Class<? extends Tipster>> tipsterListView = new JList<>(tipsterList);
    private DefaultListModel<Class<? extends Seeker>> seekerList = new DefaultListModel<>();
    private final JList<Class<? extends Seeker>> seekerListView = new JList<>(seekerList);

    public void show() {
        init();

        playButton.addActionListener(e -> {
            startGame();
        });

        listBehaviour();

        fillLists();
    }

    private void listBehaviour() {

        seekerListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tipsterListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        seekerListView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tipsterListView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ClassListMouseListener seekerListListener = new ClassListMouseListener(seekerListView, tipsterListView);
        seekerListView.addMouseListener(seekerListListener);
        seekerListView.addMouseMotionListener(seekerListListener);

        ClassListMouseListener tipsterListListener = new ClassListMouseListener(tipsterListView, seekerListView);
        tipsterListView.addMouseListener(tipsterListListener);
        tipsterListView.addMouseMotionListener(tipsterListListener);

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
        selectStrategyContainer.add(tipsterListView, Component.TOP_ALIGNMENT);
        selectStrategyContainer.add(seekerListView, Component.TOP_ALIGNMENT);

        tipsterListView.setAlignmentY(Component.TOP_ALIGNMENT);
        tipsterListView.setCellRenderer(new ClassListCellRenderer());

        seekerListView.setAlignmentY(Component.TOP_ALIGNMENT);
        seekerListView.setCellRenderer(new ClassListCellRenderer());

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
