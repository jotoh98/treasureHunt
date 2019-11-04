package com.treasure.hunt.view.main;

import com.treasure.hunt.SwingTest;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.ReflectionUtils;
import com.treasure.hunt.utils.Requires;
import com.treasure.hunt.view.swing.ClassListCellRenderer;
import com.treasure.hunt.view.swing.ClassListHoverListener;
import com.treasure.hunt.view.swing.ClassListMouseListener;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MainMenuController {
    private final JFrame jFrame = new JFrame("Treasure Hunt");
    private final JPanel rootPanel = new JPanel();
    private JPanel selectStrategyContainer = new JPanel();
    private JPanel selectContextContainer = new JPanel();
    private JButton playButton = new JButton("Play");
    private JLabel errorLabel = new JLabel();
    private DefaultListModel<Class> hiderList = new DefaultListModel<>();
    private final JList<Class> hiderListView = new JList<>(hiderList);
    private DefaultListModel<Class> gameManagerList = new DefaultListModel<>();
    private final JList<Class> gameManagerListView = new JList<>(gameManagerList);
    private DefaultListModel<Class> searcherList = new DefaultListModel<>();
    private final JList<Class> searcherListView = new JList<>(searcherList);

    public void show() {
        init();

        playButton.addActionListener(e -> {
            startGame();
        });

        setStrategyListsLAF();

        fillLists();
    }

    private void setStrategyListsLAF() {
        searcherListView.setCellRenderer(getStrategiesCellListRenderer(hiderListView));
        ClassListMouseListener searcherListListener = new ClassListMouseListener(searcherListView, Arrays.asList(hiderListView, gameManagerListView));
        searcherListView.addMouseListener(searcherListListener);
        searcherListView.addMouseMotionListener(searcherListListener);

        hiderListView.setCellRenderer(getStrategiesCellListRenderer(searcherListView));
        ClassListMouseListener hiderListListener = new ClassListMouseListener(hiderListView, Arrays.asList(searcherListView, gameManagerListView));
        hiderListView.addMouseListener(hiderListListener);
        hiderListView.addMouseMotionListener(hiderListListener);

        setGameManagerSelectionLAF();
    }

    private void fillLists() {
        Reflections searcherReflections = new Reflections("com.treasure.hunt.strategy.searcher.implementations");
        Reflections hiderReflections = new Reflections("com.treasure.hunt.strategy.hider.implementations");
        Reflections reflections = new Reflections("com.treasure.hunt.game");

        Set<Class<? extends Searcher>> allSearchers = searcherReflections.getSubTypesOf(Searcher.class);
        allSearchers = allSearchers.stream().filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())).collect(Collectors.toSet());
        allSearchers.forEach(aClass -> searcherList.addElement(aClass));

        Set<Class<? extends Hider>> allHiders = hiderReflections.getSubTypesOf(Hider.class);
        allHiders = allHiders.stream().filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())).collect(Collectors.toSet());
        allHiders.forEach(aClass -> hiderList.addElement(aClass));

        Set<Class<? extends GameManager>> gameManagerClasses = reflections.getSubTypesOf(GameManager.class);
        gameManagerClasses = gameManagerClasses.stream().filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())).collect(Collectors.toSet());
        gameManagerClasses.add(GameManager.class);
        gameManagerClasses.forEach(aClass -> gameManagerList.addElement(aClass));
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

        //ItemEditorController itemEditorController = new ItemEditorController();
        //itemEditorController.setGeometryItem(items[3]);

        canvasController.setVisible(true);
    }

    private void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initRootPanel(rootPanel);

        initStrategyContainer(selectStrategyContainer);

        initSelectContextContainer();

        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setVisible(false);
        errorLabel.setForeground(Color.RED);

        jFrame.setContentPane(rootPanel);
        jFrame.setResizable(false);
        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.setSize(new Dimension(900, 400));

    }

    private void initRootPanel(JPanel root) {
        root.setLayout(new BoxLayout(root, BoxLayout.X_AXIS));
        root.setBackground(new Color(46, 48, 50));
        root.add(selectStrategyContainer);
        root.add(selectContextContainer);
    }

    private void initStrategyContainer(JPanel container) {
        container.setAlignmentY(Component.TOP_ALIGNMENT);
        container.setLayout(new BoxLayout(selectStrategyContainer, BoxLayout.X_AXIS));
        container.setOpaque(false);

        JList[] lists = {hiderListView, searcherListView, gameManagerListView};

        for (JList list : lists) {
            container.add(list, Component.TOP_ALIGNMENT);
            list.setAlignmentY(Component.TOP_ALIGNMENT);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    private void initSelectContextContainer() {
        selectContextContainer.setAlignmentY(Component.TOP_ALIGNMENT);
        selectContextContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        selectContextContainer.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(78, 78, 78)));
        selectContextContainer.setLayout(new BoxLayout(selectContextContainer, BoxLayout.Y_AXIS));
        selectContextContainer.setBackground(new Color(0x35373A));
        selectContextContainer.add(playButton);
        selectContextContainer.add(errorLabel);
    }

    private ClassListCellRenderer getStrategiesCellListRenderer(JList<Class> opponentListView) {
        return new ClassListCellRenderer(value -> {
            Class selectedValue = opponentListView.getSelectedValue();
            if (selectedValue == null) {
                return true;
            }
            Class otherGeneric = ReflectionUtils.interfaceGenericsClass(selectedValue);
            return ReflectionUtils.interfaceGenericsClass(value).equals(otherGeneric);
        }, ReflectionUtils::genericName);
    }

    private void setGameManagerSelectionLAF() {
        gameManagerListView.setCellRenderer(getGameManagerCellListRenderer());
        ClassListHoverListener hoverListener = new ClassListHoverListener(gameManagerListView);
        gameManagerListView.addMouseListener(hoverListener);
        gameManagerListView.addMouseMotionListener(hoverListener);
    }

    private ClassListCellRenderer getGameManagerCellListRenderer() {
        return new ClassListCellRenderer(value -> {
            Class selectedSearch = searcherListView.getSelectedValue();
            Class selectedHide = hiderListView.getSelectedValue();
            if (selectedHide == null || selectedSearch == null) {
                return false;
            }
            Requires requires = (Requires) value.getAnnotation(Requires.class);
            return requires.searcher().isAssignableFrom(selectedSearch) && requires.hider().isAssignableFrom(selectedHide);
        }, value -> {
            Requires requires = (Requires) value.getAnnotation(Requires.class);
            return requires.searcher().getSimpleName() + " - " + requires.hider().getSimpleName();
        });
    }
}
