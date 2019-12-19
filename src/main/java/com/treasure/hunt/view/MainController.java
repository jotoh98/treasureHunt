package com.treasure.hunt.view;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.ReflectionUtils;
import com.treasure.hunt.utils.Requires;
import com.treasure.hunt.view.widget.BeatWidgetController;
import com.treasure.hunt.view.widget.PointInspectorController;
import com.treasure.hunt.view.widget.SaveAndLoadController;
import com.treasure.hunt.view.widget.Widget;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author jotoh
 */
@Slf4j
public class MainController {

    public SplitPane mainSplitPane;

    public Pane leftWidgetBar;
    public Pane rightWidgetBar;

    public VBox rightToolbar;
    public VBox leftToolbar;
    @FXML
    public CanvasController canvasController;
    public Pane canvas;
    @FXML
    private WidgetBarController leftWidgetBarController;
    @FXML
    private WidgetBarController rightWidgetBarController;

    @FXML
    private ToolbarController rightToolbarController;

    @FXML
    private ToolbarController leftToolbarController;

    public ComboBox<Class<? extends Searcher>> searcherList;
    public ComboBox<Class<? extends Hider>> hiderList;
    public ComboBox<Class<? extends GameEngine>> gameEngineList;
    public Button startGameButton;
    public Label logLabel;
    public Button previousButton;
    public Button nextButton;

    @Getter
    private final ObjectProperty<GameManager> gameManager = new SimpleObjectProperty<>();


    public void initialize() {
        canvasController.setGameManager(gameManager);
        setListStringConverters();
        fillLists();
        addPromptBindings();
        bindStartButtonState();
        addToolbarStyleClasses();
        bindWidgetBarVisibility();
        addPrevNextBindings();
    }

    private void addPrevNextBindings() {
        gameManager.addListener(c -> {
            if (gameManager.isNull().get()) {
                return;
            }
            nextButton.disableProperty().bind(gameManager.get().stepForwardImpossibleBinding());
            previousButton.disableProperty().bind(gameManager.get().stepBackwardImpossibleBinding());
        });
    }

    private void bindWidgetBarVisibility() {
        mainSplitPane.getItems().remove(0);
        mainSplitPane.getItems().remove(1);

        widgetBarVisibility(true, leftToolbarController);
        widgetBarVisibility(false, rightToolbarController);
    }

    private void widgetBarVisibility(boolean left, ToolbarController toolbarController) {
        final ObservableList<SplitPane.Divider> dividers = mainSplitPane.getDividers();
        AtomicReference<Node> savedBar = new AtomicReference<>(leftWidgetBar);

        if (!left) {
            savedBar.set(rightWidgetBar);
        }

        final int readPosition = left ? 0 : mainSplitPane.getItems().size() - 1;

        toolbarController.getToggleGroup().selectedToggleProperty().addListener((observableValue, oldItem, newItem) -> {
            if (newItem == null && oldItem != null) {
                savedBar.set(mainSplitPane.getItems().get(readPosition));
                mainSplitPane.getItems().remove(readPosition);
            } else if (newItem != null && oldItem == null) {
                if (left) {
                    mainSplitPane.getItems().add(0, savedBar.get());
                    dividers.get(0).setPosition(.2);
                } else {
                    mainSplitPane.getItems().add(savedBar.get());
                    dividers.get(readPosition - 1).setPosition(.8);
                }
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
        insertWidget(true, "Inspector", pointInspectorWidget.getComponent());
        Widget<SaveAndLoadController, ?> saveAndLoadWidget = new Widget<>("/layout/saveAndLoad.fxml");
        saveAndLoadWidget.getController().init(gameManager, logLabel);
        insertWidget(true, "Save & Load", saveAndLoadWidget.getComponent());
        Widget<BeatWidgetController, ?> beatWidget = new Widget<>("/layout/beatWidget.fxml");
        beatWidget.getController().init(gameManager, logLabel);
        insertWidget(true, "Game controls", beatWidget.getComponent());
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
        Reflections searcherReflections = new Reflections("com.treasure.hunt.strategy.searcher.impl");
        Reflections hiderReflections = new Reflections("com.treasure.hunt.strategy.hider.impl");
        Reflections reflections = new Reflections("com.treasure.hunt.game");

        Set<Class<? extends Searcher>> allSearchers = searcherReflections.getSubTypesOf(Searcher.class);
        allSearchers = allSearchers.stream().filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())).collect(Collectors.toSet());

        ObservableList<Class<? extends Searcher>> observableSearchers = FXCollections.observableArrayList(allSearchers);
        FilteredList<Class<? extends Searcher>> filteredSearchers = new FilteredList<>(observableSearchers);

        searcherList.setItems(filteredSearchers);

        Set<Class<? extends Hider>> allHiders = hiderReflections.getSubTypesOf(Hider.class);
        allHiders = allHiders.stream().filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())).collect(Collectors.toSet());

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

        Set<Class<? extends GameEngine>> allGameEngines = reflections.getSubTypesOf(GameEngine.class);
        allGameEngines = allGameEngines.stream().filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())).collect(Collectors.toSet());
        allGameEngines.add(GameEngine.class);
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
                //TODO maybe... ...list to fucking button cell
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

    private void insertWidget(boolean leftToolbar, String buttonText, Pane widgetBox) {
        insertWidget(leftToolbar, buttonText, widgetBox, false);
    }

    private void insertWidget(boolean leftToolbar, String buttonText, Pane widgetBox, boolean selected) {
        if (leftToolbar) {
            leftToolbarController.addButton(buttonText, selected, widgetBox);
            leftWidgetBarController.addWidget(widgetBox);
        } else {
            rightToolbarController.addButton(buttonText, selected, widgetBox);
            rightWidgetBarController.addWidget(widgetBox);
        }
    }

    public void onStartButtonClicked() {
        Class<? extends Searcher> searcherClass = searcherList.getSelectionModel().getSelectedItem();
        Class<? extends Hider> hiderClass = hiderList.getSelectionModel().getSelectedItem();
        Class<? extends GameEngine> gameEngineClass = gameEngineList.getSelectionModel().getSelectedItem();

        assert searcherClass != null;
        assert hiderClass != null;
        assert gameEngineClass != null;

        boolean initialize = gameManager.isNull().get();

        try {
            gameManager.set(new GameManager(searcherClass, hiderClass, gameEngineClass));
        } catch (Exception e) {
            log.error("Something important crashed", e);
            logLabel.setText("Could not create game");
        }

        if (initialize) {
            gameManager.addListener(change -> canvasController.drawShapes());
            initGameUI();
        }
    }

    public void initGameUI() {
        canvasController.drawShapes();
        addWidgets();
    }

    public void previousButtonClicked() {
        gameManager.get().previous();
    }

    public void nextButtonClicked() {
        gameManager.get().next();
        if (gameManager.get().isGameFinished() && gameManager.get().latestStepViewed()) {
            logLabel.setText("Game ended");
        }
    }
}
