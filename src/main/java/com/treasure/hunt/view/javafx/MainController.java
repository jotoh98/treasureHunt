package com.treasure.hunt.view.javafx;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.ReflectionUtils;
import com.treasure.hunt.utils.Requires;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.awt.ShapeWriter;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MainController {

    public Canvas canvas;
    public Pane canvasPane;
    public SplitPane mainSplitPane;
    public Pane leftWidget;
    public Pane rightWidget;
    public VBox toolbarRight;
    public VBox toolbarLeft;

    public ComboBox<Class<? extends Searcher>> searcherList;
    public ComboBox<Class<? extends Hider>> hiderList;
    public ComboBox<Class<? extends GameEngine>> gameManagerList;
    public Button startGameButton;
    public Label logLabel;
    @FXML
    private PointInspectorController pointInspectorController;
    @FXML
    private HashMap<String, PointInspectorController> pointInspectorControllers;
    private ToggleGroup leftToolbarToggle = new ToggleGroup();
    private ToggleGroup rightToolbarToggle = new ToggleGroup();

    private GameManager gameManager;

    private ShapeWriter shapeWriter = new ShapeWriter(new PointTransformation());


    public void initialize() {
        setListStringConverters();
        fillLists();
        addPromptBindings();
        bindStartButtonState();
        resizeCanvasWithSplit();
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
            public Class<? extends Searcher> fromString(String s) {
                throw new UnsupportedOperationException();
            }
        };

        StringConverter gameManagerStringConverter = new StringConverter<Class>() {

            @Override
            public String toString(Class aClass) {
                if (aClass == null) {
                    return null;
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
        gameManagerList.setConverter(gameManagerStringConverter);
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

        Set<Class<? extends GameEngine>> allGameManagers = reflections.getSubTypesOf(GameEngine.class);
        allGameManagers = allGameManagers.stream().filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())).collect(Collectors.toSet());
        allGameManagers.add(GameEngine.class);
        ObservableList<Class<? extends GameEngine>> observableGameManagers = FXCollections.observableArrayList(allGameManagers);
        FilteredList<Class<? extends GameEngine>> filteredGameManagers = new FilteredList<>(observableGameManagers);

        gameManagerList.setItems(filteredGameManagers);

        filteredGameManagers.setPredicate(aClass -> false);

        hiderList.getSelectionModel().selectedItemProperty().addListener(observable -> filteredGameManagers.setPredicate(aClass -> {
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
        addRequiredListener(gameManagerList);
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
                .or(gameManagerList.getSelectionModel().selectedItemProperty().isNull())
        );
    }

    public void onStartButtonClicked(ActionEvent actionEvent) {
        Class<? extends Searcher> searcherClass = searcherList.getSelectionModel().getSelectedItem();
        Class<? extends Hider> hiderClass = hiderList.getSelectionModel().getSelectedItem();
        Class<? extends GameEngine> gameEngineClass = gameManagerList.getSelectionModel().getSelectedItem();
        try {
            gameManager = new GameManager(searcherClass, hiderClass, gameEngineClass);
        } catch (Exception e) {
            log.error("Something important crashed", e);
        }
    }

    private void addWidget(String toolbarPosition, String buttonText, Pane widgetBox) {
        VBox toolbar = toolbarRight;
        Pane widgetWrapper = rightWidget;

        if (toolbarPosition.equals("left")) {
            toolbar = toolbarLeft;
            widgetWrapper = leftWidget;
        }

        addWidgetButton(toolbar, buttonText);
        widgetWrapper.getChildren().setAll(widgetBox);
    }

    private void addWidgetButton(VBox toolbar, String text) {
        ToggleButton toggleButton = new ToggleButton("", new Group(new Label(text)));
        if (toolbar.equals(toolbarLeft)) {
            toggleButton.setToggleGroup(leftToolbarToggle);
        } else {
            toggleButton.setToggleGroup(rightToolbarToggle);
        }
        toolbar.getChildren().add(toggleButton);
    }

    public void resizeCanvasWithSplit() {
        mainSplitPane.getDividers().forEach(divider -> divider.positionProperty().addListener(
                (observableValue, number, t1) -> {
                    log.info(String.format("%s, %s", canvas.getWidth(), canvas.getHeight()));
                })
        );

        canvas.widthProperty().addListener((observableValue, number, t1) -> {
            drawShapes(canvas.getGraphicsContext2D());
        });

        canvas.widthProperty().addListener((observableValue, number, t1) -> {
            drawShapes(canvas.getGraphicsContext2D());
        });
        canvas.setStyle("-fx-fill: white");
        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvas.widthProperty().bind(canvasPane.widthProperty());
        drawShapes(canvas.getGraphicsContext2D());
    }

    private void drawShapes(GraphicsContext gc) {
        clearCanvas();
    }

    void clearCanvas() {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
