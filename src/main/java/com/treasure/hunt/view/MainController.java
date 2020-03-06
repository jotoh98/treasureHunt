package com.treasure.hunt.view;

import com.google.common.util.concurrent.AtomicDouble;
import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.service.io.FileService;
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
import javafx.event.ActionEvent;
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
 * @author jotoh
 */
@Slf4j
public class MainController {

    @Getter
    private final ObjectProperty<GameManager> gameManager = new SimpleObjectProperty<>();
    public SplitPane mainSplitPane;
    public Pane leftWidgetBar;
    public Pane rightWidgetBar;
    public Pane bottomWidgetBar;
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
    @FXML
    private NavigationController stepViewNavigatorController;
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
        mainSplitPane.getItems().remove(0);
        mainSplitPane.getItems().remove(1);

        widgetBarVisibility(true, leftToolbarController);
        widgetBarVisibility(false, rightToolbarController);
        bottomBarVisibility();
    }

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

    private void addToolbarStyleClasses() {
        leftToolbar.getStyleClass().add("left");
        rightToolbar.getStyleClass().add("right");
    }

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

    private void addPromptBindings() {
        addRequiredListener(searcherList);
        addRequiredListener(hiderList);
        addRequiredListener(gameEngineList);
    }

    private void addRequiredListener(ComboBox comboBox) {
        comboBox.getSelectionModel().selectedItemProperty().addListener((observableValue, aClass, t1) -> {
            if (t1 == null) {
                //TODO maybe... ...list to f*****g button cell
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

    private void insertWidget(SplitPaneLocation toolbar, String buttonText, Pane widgetBox) {
        insertWidget(toolbar, buttonText, widgetBox, false);
    }

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

    public void onLoadGame(ActionEvent actionEvent) {
        FileService.getInstance().loadGameManager();
    }

    private enum SplitPaneLocation {
        EAST,
        SOUTH,
        WEST
    }
}
