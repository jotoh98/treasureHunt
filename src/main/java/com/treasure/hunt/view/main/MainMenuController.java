package com.treasure.hunt.view.main;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.Hint;
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
    private JTextArea errorLabel = new JTextArea();
    private JScrollPane errorScrollPane = new JScrollPane(errorLabel);
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
        searcherListView.setCellRenderer(getSearcherCellListRenderer(hiderListView));
        ClassListMouseListener searcherListListener = new ClassListMouseListener(searcherListView, Arrays.asList(hiderListView, gameManagerListView));
        searcherListView.addMouseListener(searcherListListener);
        searcherListView.addMouseMotionListener(searcherListListener);

        hiderListView.setCellRenderer(getHiderCellListRenderer(searcherListView));
        ClassListMouseListener hiderListListener = new ClassListMouseListener(hiderListView, Arrays.asList(searcherListView, gameManagerListView));
        hiderListView.addMouseListener(hiderListListener);
        hiderListView.addMouseMotionListener(hiderListListener);

        setGameManagerSelectionLAF();
    }

    private void fillLists() {
        Reflections searcherReflections = new Reflections("com.treasure.hunt.strategy.searcher.impl");
        Reflections hiderReflections = new Reflections("com.treasure.hunt.strategy.hider.impl");
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

    /**
     * This is executed by clicking the "Play"-button in the MainMenu
     */
    private void startGame() {
        Class<? extends Searcher> selectedSearcherClass = searcherListView.getSelectedValue();
        Class<? extends Hider> selectedHiderClass = hiderListView.getSelectedValue();
        Class<? extends GameManager> selectedGameManagerClass = gameManagerListView.getSelectedValue();

        if (selectedSearcherClass == null || selectedHiderClass == null || selectedGameManagerClass == null) {
            errorLabel.setVisible(true);
            errorLabel.setText("Please select a searcher, a hider and a game manager.");
            return;
        }

        Requires requires = selectedGameManagerClass.getAnnotation(Requires.class);

        if (!requires.searcher().isAssignableFrom(selectedSearcherClass) || !requires.hider().isAssignableFrom(selectedHiderClass)) {
            errorLabel.setVisible(true);
            errorLabel.setText("Please select a valid game manager.");
            return;
        }

        errorLabel.setVisible(false);

        try {
            MainFrameController.getInstance().onPlay(selectedSearcherClass, selectedHiderClass, selectedGameManagerClass);
        } catch (Exception e) {
            log.error("Something went wrong creating an instance of GameManager", e);
            errorLabel.setVisible(true);
            errorLabel.setText("Something went wrong");
        }
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

        // Get rid of borders
        errorScrollPane.setBorder(BorderFactory.createEmptyBorder());
        // Magically, only this colors errorScrollPane, when no text's there.
        errorScrollPane.getViewport().setBackground(new Color(0x35373A));
        // Hide scrollbars
        errorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setVisible(false);
        errorLabel.setOpaque(false);
        errorLabel.setForeground(Color.RED);
        // some copy paste,
        // preventing the errorLabel to get out of sight
        errorLabel.setWrapStyleWord(true);
        errorLabel.setLineWrap(true);
        errorLabel.setEditable(false);
        errorLabel.setFocusable(false);
        //

        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setContentPane(rootPanel);
        jFrame.setResizable(false);
        jFrame.pack();
        jFrame.setVisible(true);

    }

    private void initRootPanel(JPanel root) {
        root.setLayout(new BoxLayout(root, BoxLayout.X_AXIS));
        root.setBackground(new Color(46, 48, 50));
        root.add(selectStrategyContainer);
        root.add(selectContextContainer);
        root.setPreferredSize(new Dimension(900, 400));
    }

    /**
     * This initializes the different lists of hider, searcher, gameManagers.
     *
     * @param container The container, getting the different "lists"
     */
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

    /**
     * This initializes the container on the right,
     * containing the "Play"-Button and the errorLabel.
     */
    private void initSelectContextContainer() {
        selectContextContainer.setAlignmentY(Component.TOP_ALIGNMENT);
        selectContextContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        selectContextContainer.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(78, 78, 78)));
        selectContextContainer.setLayout(new BoxLayout(selectContextContainer, BoxLayout.Y_AXIS));
        selectContextContainer.setBackground(new Color(0x35373A));
        selectContextContainer.add(playButton);
        selectContextContainer.add(errorScrollPane);
    }

    private ClassListCellRenderer getHiderCellListRenderer(JList<Class> opponentListView) {
        return new ClassListCellRenderer(value -> {
            Class selectedValue = opponentListView.getSelectedValue();
            if (selectedValue == null) {
                return true;
            }
            Class<Hint> otherGeneric = ReflectionUtils.interfaceGenericsClass(selectedValue);
            return otherGeneric.isAssignableFrom(ReflectionUtils.interfaceGenericsClass(value));
        }, ReflectionUtils::genericName);
    }

    private ClassListCellRenderer getSearcherCellListRenderer(JList<Class> opponentListView) {
        return new ClassListCellRenderer(value -> {
            Class selectedValue = opponentListView.getSelectedValue();
            if (selectedValue == null) {
                return true;
            }
            Class otherGeneric = ReflectionUtils.interfaceGenericsClass(selectedValue);
            return ReflectionUtils.interfaceGenericsClass(value).isAssignableFrom(otherGeneric);
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
