package com.treasure.hunt.view;

import com.google.common.util.concurrent.AtomicDouble;
import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.EventBusUtils;
import com.treasure.hunt.utils.ReflectionUtils;
import com.treasure.hunt.utils.Requires;
import com.treasure.hunt.view.widget.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Controller for the main layout.
 *
 * @author jotoh
 */
@Slf4j
public class MainController {

    /**
     * The {@link GameManager} instance for the run.
     */
    @Getter
    private final ObjectProperty<GameManager> gameManager = new SimpleObjectProperty<>();

    /**
     * The central vertical split pane.
     */
    public SplitPane mainSplitPane;

    /**
     * Left widget bar for widgets containers.
     */
    public Pane leftWidgetBar;
    /**
     * Left toolbar for widgets buttons.
     */
    public VBox leftToolbar;
    /**
     * Right widget bar for widgets containers.
     */
    public Pane rightWidgetBar;
    /**
     * Left toolbar for widgets buttons.
     */
    public VBox rightToolbar;
    /**
     * Bottom widget bar for widgets containers.
     */
    public Pane bottomWidgetBar;
    /**
     * Bottom toolbar for widgets buttons.
     */
    public HBox bottomToolbar;
    /**
     * Instance of the {@link CanvasController} associated with the {@link javafx.scene.canvas.Canvas}.
     */
    @FXML
    public CanvasController canvasController;
    /**
     * Main split pane for the left and right widgets and the canvas.
     */
    public SplitPane mainVerticalSplitPane;
    /**
     * List of available searchers.
     */
    public ComboBox<Class<? extends Searcher>> searcherList;
    /**
     * List of available hiders.
     */
    public ComboBox<Class<? extends Hider>> hiderList;
    /**
     * List of available game engines.
     */
    public ComboBox<Class<? extends GameEngine>> gameEngineList;
    /**
     * The button to initialize the game on the ui.
     */
    public Button startGameButton;
    /**
     * The label used for logging outputs on the bottom left.
     */
    public Label logLabel;

    /**
     * Navigator for the view.
     * Changes the step view.
     */
    public HBox stepViewNavigator;
    /**
     * Controller of the left widget bar.
     */
    @FXML
    private WidgetBarController leftWidgetBarController;
    /**
     * Controller of the left toolbar.
     */
    @FXML
    private ToolbarController leftToolbarController;
    /**
     * Controller of the right widget bar.
     */
    @FXML
    private WidgetBarController rightWidgetBarController;
    /**
     * Controller of the right toolbar.
     */
    @FXML
    private ToolbarController rightToolbarController;
    /**
     * Controller of the lower widget bar.
     */
    @FXML
    private WidgetBarController bottomWidgetBarController;
    /**
     * Controller of the lower toolbar.
     */
    @FXML
    private ToolbarController bottomToolbarController;
    /**
     * Controller for the step view navigation.
     */
    @FXML
    private NavigationController stepViewNavigatorController;

    /**
     * Upper left versioning label.
     */
    @FXML
    private Label versionLabel;

    /**
     * Initializer binding all the components together.
     */
    public void initialize() {
        canvasController.setGameManager(gameManager);
        String implementationVersion = getClass().getPackage().getImplementationVersion();
        versionLabel.setText(implementationVersion == null ? "snapshot" : "v" + implementationVersion);
        setListStringConverters();
        fillLists();
        addPromptBindings();
        bindStartButtonState();
        addToolbarStyleClasses();
        bindWidgetBarVisibility();
        addBindingsToGameManager();
        listenToGameMangerLoad();
        listenToLogLabelEvent();
        addGameIndependentWidgets();
    }

    /**
     * Add widgets which are independent of a game instance.
     */
    private void addGameIndependentWidgets() {
        Widget<StatisticTableController, ?> statisticsTableWidget = new Widget<>("/layout/statisticsTable.fxml");
        statisticsTableWidget.getController().init(gameManager, searcherList, hiderList, gameEngineList);
        insertWidget(SplitPaneLocation.SOUTH, "Statistics", statisticsTableWidget.getComponent(), false);

        Widget<SaveAndLoadController, ?> saveAndLoadWidget = new Widget<>("/layout/saveAndLoad.fxml");
        saveAndLoadWidget.getController().init(gameManager);
        insertWidget(SplitPaneLocation.WEST, "Save & Load", saveAndLoadWidget.getComponent(), true);

        Widget<PreferencesWidgetController, ?> preferencesWidgetControllerPaneWidget = new Widget<>("/layout/preferencesWidget.fxml");
        insertWidget(SplitPaneLocation.WEST, "Preferences", preferencesWidgetControllerPaneWidget.getComponent(), true);
    }

    /**
     * Bind the log label event to the log label.
     */
    private void listenToLogLabelEvent() {
        EventBusUtils.LOG_LABEL_EVENT.addListener(logLabelMessage -> Platform.runLater(() -> logLabel.setText(logLabelMessage)));
    }

    /**
     * Bind the event of the game manager being loaded to the initialisation sequence.
     *
     * @see MainController#initGameManager(GameManager)
     */
    private void listenToGameMangerLoad() {
        EventBusUtils.GAME_MANAGER_LOADED_EVENT.addListener(loadedGameManager -> Platform.runLater(() -> {
            try {
                initGameManager(loadedGameManager);
            } catch (Exception e) {
                log.error("Error loading GameManger in UI", e);
                EventBusUtils.LOG_LABEL_EVENT.trigger("Error loading GameManger in UI from file");
            }
        }));
    }

    /**
     * Binds the visibility of the widget containers to the individual button toggle groups.
     * If no button is active, then the widget container will be invisible.
     */
    private void bindWidgetBarVisibility() {
        mainSplitPane.getItems().remove(0);
        mainSplitPane.getItems().remove(mainSplitPane.getItems().size() - 1);

        widgetBarVisibility(true, leftToolbarController);
        widgetBarVisibility(false, rightToolbarController);
        bottomBarVisibility();
    }

    /**
     * Add property bindings to the {@link GameManager}s members.
     */
    private void addBindingsToGameManager() {
        gameManager.addListener(c -> {
            if (gameManager.isNull().get()) {
                return;
            }
            gameManager.get().getFinishedProperty().addListener(invalidation -> EventBusUtils.LOG_LABEL_EVENT.trigger("Game ended"));
        });
        gameManager.bindBidirectional(stepViewNavigatorController.getGameManager());
    }

    /**
     * Binds the visibility of either the left or the right widget bar.
     *
     * @param left              whether the binding ovvurs on the left or the right bar
     * @param toolbarController the associated toolbar controller
     */
    private void widgetBarVisibility(boolean left, ToolbarController toolbarController) {
        final ObservableList<SplitPane.Divider> dividers = mainSplitPane.getDividers();
        AtomicReference<Node> savedBar = new AtomicReference<>(leftWidgetBar);

        AtomicDouble leftSplit = new AtomicDouble(.2);
        AtomicDouble rightSplit = new AtomicDouble(.8);

        if (!left) {
            savedBar.set(rightWidgetBar);
        }

        toolbarController.getToggleGroup().selectedToggleProperty().addListener((observableValue, oldItem, newItem) -> {
            final int readPosition = left ? 0 : mainSplitPane.getItems().size() - 1;

            if (newItem == null) {
                if (left) {
                    leftSplit.set(mainSplitPane.getDividerPositions()[0]);
                } else {
                    rightSplit.set(mainSplitPane.getDividerPositions()[mainSplitPane.getDividers().size() - 1]);
                }
                savedBar.set(mainSplitPane.getItems().get(readPosition));
                mainSplitPane.getItems().remove(readPosition);
            } else if (oldItem == null) {
                if (left) {
                    mainSplitPane.getItems().add(0, savedBar.get());
                    dividers.get(0).setPosition(leftSplit.get());
                } else {
                    mainSplitPane.getItems().add(savedBar.get());
                    dividers.get(readPosition).setPosition(rightSplit.get());
                }
            }
        });
    }

    /**
     * Binds the visibility of the bottom widget bar.
     */
    private void bottomBarVisibility() {
        mainVerticalSplitPane.getItems().remove(1);
        AtomicDouble slider = new AtomicDouble(.2);
        bottomToolbarController
                .getToggleGroup()
                .selectedToggleProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        slider.set(mainVerticalSplitPane.getDividerPositions()[0]);
                        mainVerticalSplitPane.getItems().remove(1);
                    } else if (oldValue == null) {
                        mainVerticalSplitPane.getItems().add(bottomWidgetBar);
                        mainVerticalSplitPane.setDividerPosition(0, slider.get());
                    }
                });
    }

    /**
     * Adds css style classes to the toolbars.
     */
    private void addToolbarStyleClasses() {
        leftToolbar.getStyleClass().add("left");
        rightToolbar.getStyleClass().add("right");
    }

    /**
     * Add game dependent widgets.
     * In general, these analyse the behaviour of the game.
     */
    private void addWidgets() {
        Widget<PointInspectorController, ?> pointInspectorWidget = new Widget<>("/layout/pointInspector.fxml");
        pointInspectorWidget.getController().init(gameManager);
        insertWidget(SplitPaneLocation.WEST, "Inspector", pointInspectorWidget.getComponent());

        Widget<BeatWidgetController, ?> beatWidget = new Widget<>("/layout/beatWidget.fxml");
        beatWidget.getController().init(gameManager);
        insertWidget(SplitPaneLocation.WEST, "Game controls", beatWidget.getComponent());

        Widget<StatisticsWidgetController, ?> statisticsWidget = new Widget<>("/layout/statisticsWidget.fxml");
        statisticsWidget.getController().init(gameManager);
        insertWidget(SplitPaneLocation.WEST, "Statistics", statisticsWidget.getComponent());

        Widget<StatusMessageWidgetController, ?> statusWidget = new Widget<>("/layout/statusMessageWidget.fxml");
        statusWidget.getController().init(gameManager);
        insertWidget(SplitPaneLocation.EAST, "Status", statusWidget.getComponent());

        Widget<ScaleController, ?> scaleWidget = new Widget<>("/layout/scaling.fxml");
        scaleWidget.getController().init(canvasController);
        insertWidget(SplitPaneLocation.EAST, "Navigator", scaleWidget.getComponent());
    }

    /**
     * Set the converters of the game init drop downs.
     * Each hider/searcher and game engine drop down at the top gets one.
     */
    private void setListStringConverters() {
        StringConverter classStringConverter = new StringConverter<Class>() {
            @Override
            public String toString(Class aClass) {
                if (aClass == null) {
                    return null;
                }
                return String.format("%s (%s)", aClass.getSimpleName(), ReflectionUtils.genericName(aClass));
            }

            @Override
            public Class fromString(String s) {
                throw new UnsupportedOperationException();
            }
        };

        StringConverter<Class<? extends GameEngine>> gameManagerStringConverter = new StringConverter<>() {

            @Override
            public String toString(Class aClass) {
                if (aClass == null) {
                    return "null";
                }
                Requires requires = (Requires) aClass.getAnnotation(Requires.class);
                return String.format(
                        "%s (%s - %s)",
                        aClass.getSimpleName(),
                        requires.searcher().getSimpleName(),
                        requires.hider().getSimpleName()
                );
            }

            @Override
            public Class fromString(String s) {
                throw new UnsupportedOperationException();
            }
        };

        searcherList.setConverter(classStringConverter);
        hiderList.setConverter(classStringConverter);
        gameEngineList.setConverter(gameManagerStringConverter);
    }

    /**
     * Fill the game init drop downs at the top via reflections.
     */
    private void fillLists() {

        Set<Class<? extends Searcher>> allSearchers = ReflectionUtils.getAllSearchers();
        Set<Class<? extends Hider>> allHiders = ReflectionUtils.getAllHiders();
        Set<Class<? extends GameEngine>> allGameEngines = ReflectionUtils.getAllGameEngines();

        ObservableList<Class<? extends Searcher>> observableSearchers = FXCollections.observableArrayList(allSearchers);
        FilteredList<Class<? extends Searcher>> filteredSearchers = new FilteredList<>(observableSearchers);

        searcherList.setItems(filteredSearchers);

        ObservableList<Class<? extends Hider>> observableHiders = FXCollections.observableArrayList(allHiders);
        FilteredList<Class<? extends Hider>> filteredHiders = new FilteredList<>(observableHiders);

        hiderList.setItems(filteredHiders);

        filteredHiders.setPredicate(aClass -> false);

        searcherList.getSelectionModel().selectedItemProperty().addListener(observable ->
                filteredHiders.setPredicate(aClass -> {
                    Class<? extends Searcher> selectedSearcher = searcherList.getSelectionModel().getSelectedItem();
                    if (selectedSearcher == null) {
                        return false;
                    }
                    return ReflectionUtils.interfaceGenericsClass(selectedSearcher).isAssignableFrom(ReflectionUtils.interfaceGenericsClass(aClass));
                })
        );

        ObservableList<Class<? extends GameEngine>> observableGameEngines = FXCollections.observableArrayList(allGameEngines);
        FilteredList<Class<? extends GameEngine>> filteredGameEngines = new FilteredList<>(observableGameEngines);

        gameEngineList.setItems(filteredGameEngines);

        filteredGameEngines.setPredicate(aClass -> false);

        hiderList.getSelectionModel().selectedItemProperty().addListener(observable -> filteredGameEngines.setPredicate(aClass -> {
            Class selectedSearch = searcherList.getSelectionModel().getSelectedItem();
            Class selectedHide = hiderList.getSelectionModel().getSelectedItem();
            if (selectedHide == null || selectedSearch == null) {
                return false;
            }
            Requires requires = aClass.getAnnotation(Requires.class);
            return requires.searcher().isAssignableFrom(selectedSearch) && requires.hider().isAssignableFrom(selectedHide);
        }));
    }

    /**
     * Adds an required css class behaviour to the init drop downs.
     */
    private void addPromptBindings() {
        addRequiredListener(searcherList);
        addRequiredListener(hiderList);
        addRequiredListener(gameEngineList);
    }

    /**
     * Adds an required css class behaviour a drop down.
     *
     * @param comboBox the drop down to get a required css class
     */
    private void addRequiredListener(ComboBox comboBox) {
        comboBox.getSelectionModel().selectedItemProperty().addListener((observableValue, aClass, t1) -> {
            if (t1 == null) {
                comboBox.getStyleClass().add("required");
            } else {
                comboBox.getStyleClass().remove("required");
            }
        });
    }

    /**
     * Bind the init button active state to all drop downs being set.
     */
    private void bindStartButtonState() {
        startGameButton.disableProperty().bind(searcherList.getSelectionModel().selectedItemProperty().isNull()
                .or(hiderList.getSelectionModel().selectedItemProperty().isNull())
                .or(gameEngineList.getSelectionModel().selectedItemProperty().isNull())
        );
    }

    /**
     * Insert a widget to the gui.
     * Sets the selected property to false by default.
     *
     * @param toolbar    location of toolbar to add a button to
     * @param buttonText the button text
     * @param widgetBox  the widget box
     */
    private void insertWidget(SplitPaneLocation toolbar, String buttonText, Pane widgetBox) {
        insertWidget(toolbar, buttonText, widgetBox, false);
    }

    /**
     * Insert a widget to the gui.
     *
     * @param toolbar    location of toolbar to add a button to
     * @param buttonText the button text
     * @param widgetBox  the widget box
     * @param selected   whether or not to select the widget button and thus to show the widget
     */
    private void insertWidget(SplitPaneLocation toolbar, String buttonText, Pane widgetBox, boolean selected) {

        switch (toolbar) {
            case WEST:
                leftToolbarController.addButton(buttonText, selected, widgetBox);
                leftWidgetBarController.addWidget(widgetBox);
                break;
            case EAST:
                rightToolbarController.addButton(buttonText, selected, widgetBox);
                rightWidgetBarController.addWidget(widgetBox);
                break;
            case SOUTH:
                bottomToolbarController.addButton(buttonText, selected, widgetBox);
                bottomWidgetBarController.addWidget(widgetBox);
        }
    }

    /**
     * Behaviour for the start button being clicked.
     * Initializes the game manager.
     */
    public void onStartButtonClicked() {
        Class<? extends Searcher> searcherClass = searcherList.getSelectionModel().getSelectedItem();
        Class<? extends Hider> hiderClass = hiderList.getSelectionModel().getSelectedItem();
        Class<? extends GameEngine> gameEngineClass = gameEngineList.getSelectionModel().getSelectedItem();

        assert searcherClass != null;
        assert hiderClass != null;
        assert gameEngineClass != null;
        try {
            initGameManager(new GameManager(searcherClass, hiderClass, gameEngineClass));
        } catch (Exception e) {
            log.error("Something important crashed", e);
            EventBusUtils.LOG_LABEL_EVENT.trigger("Could not create game");
        }
    }

    /**
     * Initializes the game manager.
     *
     * @param gameManagerInstance instance of game manager to initialize.
     */
    private void initGameManager(GameManager gameManagerInstance) {
        gameManagerInstance.init();
        boolean initialize = gameManager.isNull().get();

        gameManager.set(gameManagerInstance);
        EventBusUtils.LOG_LABEL_EVENT.trigger("Game initialized");

        if (initialize) {
            gameManager.addListener(change -> canvasController.drawShapes());
            initGameUI();
        }
    }

    /**
     * Initialize the gui elements dependent on the game manager.
     */
    public void initGameUI() {
        canvasController.drawShapes();
        addWidgets();
    }

    /**
     * Available locations for the widgets.
     */
    private enum SplitPaneLocation {
        EAST,
        SOUTH,
        WEST
    }
}
