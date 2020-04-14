package com.treasure.hunt.view;

import com.google.common.util.concurrent.AtomicDouble;
import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.service.settings.Session;
import com.treasure.hunt.service.settings.SettingsService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.EventBusUtils;
import com.treasure.hunt.utils.ReflectionUtils;
import com.treasure.hunt.utils.Requires;
import com.treasure.hunt.view.settings.SettingsWindow;
import com.treasure.hunt.view.widget.*;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.*;
import javafx.util.Pair;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.function.Consumer;

/**
 * @author jotoh
 */
@Slf4j
public class MainController {

    @Getter
    private final ObjectProperty<GameManager> gameManager = new SimpleObjectProperty<>();
    public SplitPane mainSplitPane;
    public SplitPane leftWidgetBar;
    public SplitPane rightWidgetBar;
    public SplitPane bottomWidgetBar;
    public VBox rightToolbar;
    public VBox leftToolbar;
    public HBox bottomToolbar;
    public Pane canvas;
    @FXML
    public CanvasController canvasController;
    /**
     * Navigator for the view.
     * Changes the step view.
     */
    public HBox stepViewNavigator;
    public SplitPane mainVerticalSplitPane;
    public ComboBox<Class<? extends Searcher>> searcherList;
    public ComboBox<Class<? extends Hider>> hiderList;
    public ComboBox<Class<? extends GameEngine>> gameEngineList;
    public Button startGameButton;
    public Label logLabel;
    public Group popupGroup;
    public StackPane mainRoot;
    @FXML
    private StepNavigationController stepViewNavigatorController;
    @FXML
    private Label versionLabel;
    @FXML
    private WidgetBarController leftWidgetBarController;
    @FXML
    private WidgetBarController rightWidgetBarController;
    @FXML
    private WidgetBarController bottomWidgetBarController;
    @FXML
    private ToolbarController leftToolbarController;
    @FXML
    private ToolbarController rightToolbarController;
    @FXML
    private ToolbarController bottomToolbarController;
    private BooleanBinding leftVisibleBinding;
    private BooleanBinding rightVisibleBinding;
    private BooleanBinding bottomVisibleBinding;

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
        bindWidgetControllers();
        addGameIndependentWidgets();
        bindAbsoluteSplitPane(mainSplitPane);
        bindAbsoluteSplitPane(mainVerticalSplitPane);
        setUpPopUpPane();
        insertSessionConfiguration();
        EventBusUtils.INNER_POP_UP_EVENT.addListener(this::newInnerPopUp);
        EventBusUtils.INNER_POP_UP_EVENT_CLOSE.addListener(this::closePopUp);
    }

    private void bindWidgetControllers() {
        leftWidgetBarController.bindToggleGroups(leftToolbarController);
        rightWidgetBarController.bindToggleGroups(rightToolbarController);
        bottomWidgetBarController.bindToggleGroups(bottomToolbarController);
    }

    private void closePopUp(Void aVoid) {
        popupGroup.setVisible(false);
    }

    private void newInnerPopUp(Pair<Node, Pair<Double, Double>> args) {
        Bounds boundsInLocal = mainRoot.getBoundsInLocal();
        Bounds bounds = mainRoot.localToScreen(boundsInLocal);
        popupGroup.setTranslateX(args.getValue().getKey() - bounds.getMinX());
        popupGroup.setTranslateY(args.getValue().getValue() - bounds.getMinY());
        popupGroup.setVisible(true);
        popupGroup.getChildren().setAll(args.getKey());
    }

    private void setUpPopUpPane() {
        popupGroup.managedProperty().bind(popupGroup.visibleProperty());
    }

    public void saveSession() {
        if (!SettingsService.getInstance().getSettings().isPreserveConfiguration()) {
            return;
        }

        final Session session = SettingsService.getInstance().getSession();

        session.setSearcher(searcherList.getSelectionModel().getSelectedItem());
        session.setHider(hiderList.getSelectionModel().getSelectedItem());
        session.setEngine(gameEngineList.getSelectionModel().getSelectedItem());

        bindListSelectionToSession(searcherList, session::setSearcher);
        bindListSelectionToSession(hiderList, session::setHider);
        bindListSelectionToSession(gameEngineList, session::setEngine);
    }

    private <T> void bindListSelectionToSession(final ComboBox<T> searcherList, final Consumer<T> consumer) {
        searcherList.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
    }

    private void insertSessionConfiguration() {
        if (SettingsService.getInstance().getSettings().isPreserveConfiguration()) {
            Session session = SettingsService.getInstance().getSession();
            searcherList.getSelectionModel().select(session.getSearcher());
            hiderList.getSelectionModel().select(session.getHider());
            gameEngineList.getSelectionModel().select(session.getEngine());
        }
    }

    private void addGameIndependentWidgets() {
        Widget<StatisticTableController, ?> statisticsTableWidget = new Widget<>("/layout/statisticsTable.fxml");
        statisticsTableWidget.getController().init(gameManager, searcherList, hiderList, gameEngineList);
        insertWidget(SplitPaneLocation.BOTTOM_LEFT, "Statistics", statisticsTableWidget.getComponent(), false);

        Widget<SaveAndLoadController, ?> saveAndLoadWidget = new Widget<>("/layout/saveAndLoad.fxml");
        saveAndLoadWidget.getController().init(gameManager);
        insertWidget(SplitPaneLocation.LEFT_UPPER, "Save & Load", saveAndLoadWidget.getComponent(), true);

        Widget<PreferencesWidgetController, ?> preferencesWidgetControllerPaneWidget = new Widget<>("/layout/preferencesWidget.fxml");
        preferencesWidgetControllerPaneWidget.getController().init(
                searcherList.getSelectionModel().selectedItemProperty(),
                hiderList.getSelectionModel().selectedItemProperty(),
                gameEngineList.getSelectionModel().selectedItemProperty(),
                gameManager
        );
        insertWidget(SplitPaneLocation.LEFT_LOWER, "Preferences", preferencesWidgetControllerPaneWidget.getComponent(), true);
    }

    private void listenToLogLabelEvent() {
        EventBusUtils.LOG_LABEL_EVENT.addListener(logLabelMessage -> Platform.runLater(() -> logLabel.setText(logLabelMessage)));
    }

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

    private void bindWidgetBarVisibility() {
        leftVisibleBinding = leftToolbarController.visibleBinding();
        rightVisibleBinding = rightToolbarController.visibleBinding();
        bottomVisibleBinding = bottomToolbarController.visibleBinding();

        bindWidgetBarVisibility(leftVisibleBinding, mainSplitPane, leftWidgetBar, true);
        bindWidgetBarVisibility(rightVisibleBinding, mainSplitPane, rightWidgetBar, false);
        bindWidgetBarVisibility(bottomVisibleBinding, mainVerticalSplitPane, bottomWidgetBar, false);
    }

    private void bindWidgetBarVisibility(BooleanBinding visibleBinding, SplitPane wrapper, SplitPane widgetBar, boolean first) {
        final ObservableList<Node> items = wrapper.getItems();
        final AtomicDouble dividerPosition = new AtomicDouble(first ? .2 : .8);

        visibleBinding.addListener((observable, wasVisible, isVisible) -> {
            if (wasVisible == isVisible) {
                return;
            }
            if (!isVisible) {
                int dividerIndex = first || items.size() < 3 ? 0 : 1;
                dividerPosition.set(wrapper.getDividerPositions()[dividerIndex]);
                items.remove(widgetBar);
            } else if (!items.contains(widgetBar)) {
                if (first) {
                    items.add(0, widgetBar);
                    wrapper.setDividerPosition(0, dividerPosition.get());
                } else {
                    items.add(widgetBar);
                    wrapper.setDividerPosition(items.size() < 3 ? 0 : 1, dividerPosition.get());
                }
            }
        });

        if (!visibleBinding.get()) {
            items.remove(widgetBar);
        }
    }

    private void bindAbsoluteSplitPane(SplitPane splitPane) {
        (splitPane.getOrientation() == Orientation.VERTICAL ? splitPane.heightProperty() : splitPane.widthProperty()).addListener(
                (observable, oldWidth, newWidth) -> splitPane.getDividers().forEach(
                        divider -> {
                            if (oldWidth.doubleValue() == 0 || newWidth.doubleValue() == 0) {
                                return;
                            }
                            divider.setPosition(oldWidth.doubleValue() / newWidth.doubleValue() * divider.getPosition());
                        }
                )
        );
    }

    private void addToolbarStyleClasses() {
        leftToolbar.getStyleClass().add("left");
        rightToolbar.getStyleClass().add("right");
    }

    private void addWidgets() {
        Widget<ClickInspectorController, ?> pointInspectorWidget = new Widget<>("/layout/clickedInspector.fxml");
        pointInspectorWidget.getController().init();
        insertWidget(SplitPaneLocation.LEFT_LOWER, "Inspector", pointInspectorWidget.getComponent());

        Widget<BeatWidgetController, ?> beatWidget = new Widget<>("/layout/beatWidget.fxml");
        beatWidget.getController().init(gameManager);
        insertWidget(SplitPaneLocation.LEFT_LOWER, "Game controls", beatWidget.getComponent());

        Widget<StatisticsWidgetController, ?> statisticsWidget = new Widget<>("/layout/statisticsWidget.fxml");
        statisticsWidget.getController().init(gameManager);
        insertWidget(SplitPaneLocation.LEFT_UPPER, "Statistics", statisticsWidget.getComponent());

        Widget<StatusMessageWidgetController, ?> statusWidget = new Widget<>("/layout/statusMessageWidget.fxml");
        statusWidget.getController().init(gameManager);
        insertWidget(SplitPaneLocation.RIGHT_UPPER, "Status", statusWidget.getComponent());

        Widget<NavigatorController, ?> scaleWidget = new Widget<>("/layout/navigator.fxml");
        scaleWidget.getController().init(gameManager, canvasController);
        insertWidget(SplitPaneLocation.RIGHT_LOWER, "Navigator", scaleWidget.getComponent(), true);

        Widget<HistoryController, ?> historyWidget = new Widget<>("/layout/history.fxml");
        historyWidget.getController().init(gameManager);
        insertWidget(SplitPaneLocation.RIGHT_UPPER, "History", historyWidget.getComponent());
    }

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

    private void fillLists() {

        Set<Class<? extends Searcher>> allSearchers = ReflectionUtils.getAllSearchers();
        Set<Class<? extends Hider>> allHiders = ReflectionUtils.getAllHiders();
        Set<Class<? extends GameEngine>> allGameEngines = ReflectionUtils.getAllGameEngines();

        ObservableList<Class<? extends Searcher>> observableSearchers = FXCollections.observableArrayList(allSearchers).sorted();
        FilteredList<Class<? extends Searcher>> filteredSearchers = new FilteredList<>(observableSearchers);

        searcherList.setItems(filteredSearchers);

        ObservableList<Class<? extends Hider>> observableHiders = FXCollections.observableArrayList(allHiders).sorted();
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

    private void addPromptBindings() {
        addRequiredListener(searcherList);
        addRequiredListener(hiderList);
        addRequiredListener(gameEngineList);
    }

    private void addRequiredListener(ComboBox comboBox) {
        comboBox.getSelectionModel().selectedItemProperty().addListener((observableValue, aClass, t1) -> {
            if (t1 == null) {
                comboBox.getStyleClass().add("required");
            } else {
                comboBox.getStyleClass().remove("required");
            }
        });
    }

    private void bindStartButtonState() {
        startGameButton.disableProperty().bind(searcherList.getSelectionModel().selectedItemProperty().isNull()
                .or(hiderList.getSelectionModel().selectedItemProperty().isNull())
                .or(gameEngineList.getSelectionModel().selectedItemProperty().isNull())
        );
    }

    private void insertWidget(SplitPaneLocation toolbar, String buttonText, Region widgetBox) {
        insertWidget(toolbar, buttonText, widgetBox, false);
    }

    private void insertWidget(SplitPaneLocation location, String buttonText, Region widgetBox, boolean selected) {
        boolean first = location == SplitPaneLocation.BOTTOM_LEFT || location == SplitPaneLocation.LEFT_UPPER || location == SplitPaneLocation.RIGHT_UPPER;
        selectToolbarController(location).addButton(first, buttonText, selected, widgetBox);
        selectWidgetController(location).addWidget(first, widgetBox);
    }

    private WidgetBarController selectWidgetController(SplitPaneLocation location) {
        switch (location) {
            case LEFT_UPPER:
            case LEFT_LOWER:
                return leftWidgetBarController;
            case RIGHT_UPPER:
            case RIGHT_LOWER:
                return rightWidgetBarController;
        }
        return bottomWidgetBarController;
    }

    private ToolbarController selectToolbarController(SplitPaneLocation location) {
        switch (location) {
            case LEFT_UPPER:
            case LEFT_LOWER:
                return leftToolbarController;
            case RIGHT_UPPER:
            case RIGHT_LOWER:
                return rightToolbarController;
        }
        return bottomToolbarController;
    }

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

    public void initGameUI() {
        canvasController.drawShapes();
        addWidgets();
    }

    public void openSettingsWindow() {
        try {
            SettingsWindow.show();
        } catch (Exception e) {
            log.error("Could not open the settings", e);
        }
    }

    private enum SplitPaneLocation {
        LEFT_UPPER,
        LEFT_LOWER,
        RIGHT_UPPER,
        RIGHT_LOWER,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
