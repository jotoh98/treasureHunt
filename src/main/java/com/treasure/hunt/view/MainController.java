package com.treasure.hunt.view;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.jts.AdvancedShapeWriter;
import com.treasure.hunt.jts.PointTransformation;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.ReflectionUtils;
import com.treasure.hunt.utils.Requires;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.jfree.fx.FXGraphics2D;
import org.locationtech.jts.math.Vector2D;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jotoh
 */
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
    public ComboBox<Class<? extends GameEngine>> gameEngineList;
    public Button startGameButton;
    public Label logLabel;
    public Button previousButton;
    public Button nextButton;

    @FXML
    private PointInspectorController pointInspectorController;
    @FXML
    private HashMap<String, PointInspectorController> pointInspectorControllers;
    private ToggleGroup leftToolbarToggle = new ToggleGroup();
    private ToggleGroup rightToolbarToggle = new ToggleGroup();

    private GameManager gameManager;

    private PointTransformation transformation = new PointTransformation();
    private AdvancedShapeWriter shapeWriter = new AdvancedShapeWriter(transformation);

    private FXGraphics2D graphics2D;

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
            public Class fromString(String s) {
                throw new UnsupportedOperationException();
            }
        };

        StringConverter<Class<? extends GameEngine>> gameManagerStringConverter = new StringConverter<>() {

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

        Set<Class<? extends GameEngine>> allGameManagers = reflections.getSubTypesOf(GameEngine.class);
        allGameManagers = allGameManagers.stream().filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())).collect(Collectors.toSet());
        allGameManagers.add(GameEngine.class);
        ObservableList<Class<? extends GameEngine>> observableGameManagers = FXCollections.observableArrayList(allGameManagers);
        FilteredList<Class<? extends GameEngine>> filteredGameManagers = new FilteredList<>(observableGameManagers);

        gameEngineList.setItems(filteredGameManagers);

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

    private Vector2D dragStart = new Vector2D();

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

    private Vector2D offsetBackup = new Vector2D();

    public void onStartButtonClicked() {
        Class<? extends Searcher> searcherClass = searcherList.getSelectionModel().getSelectedItem();
        Class<? extends Hider> hiderClass = hiderList.getSelectionModel().getSelectedItem();
        Class<? extends GameEngine> gameEngineClass = gameEngineList.getSelectionModel().getSelectedItem();

        assert searcherClass != null;
        assert hiderClass != null;
        assert gameEngineClass != null;

        try {
            gameManager = new GameManager(searcherClass, hiderClass, gameEngineClass);
            graphics2D = new FXGraphics2D(canvas.getGraphicsContext2D());
            gameManager.addListener(change -> drawShapes());
            drawShapes();
        } catch (Exception e) {
            log.error("Something important crashed", e);
            logLabel.setText("Could not create game");
        }
    }

    public void previousButtonClicked() {
        gameManager.previous();
        drawShapes();
        if (gameManager.isFirstStepShown()) {
            previousButton.setDisable(true);
        }
        nextButton.setDisable(false);
    }

    public void nextButtonClicked() {
        gameManager.next();
        drawShapes();
        if (gameManager.isGameFinished() && gameManager.isSimStepLatest()) {
            nextButton.setDisable(true);
            logLabel.setText("Game ended");
        }
        previousButton.setDisable(false);
    }

    public void resizeCanvasWithSplit() {
        canvas.widthProperty().addListener((observableValue, number, t1) -> drawShapes());

        canvas.widthProperty().addListener((observableValue, number, t1) -> drawShapes());

        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvas.widthProperty().bind(canvasPane.widthProperty());
    }

    private void drawShapes() {
        if (gameManager != null) {
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gameManager.getGeometryItems().forEach(geometryItem ->
                    geometryItem.draw(graphics2D, shapeWriter)
            );
        }
    }

    public void onCanvasClicked(MouseEvent mouseEvent) {
        if (gameManager == null) {
            return;
        }
        offsetBackup = transformation.getOffset();
        dragStart = Vector2D.create(mouseEvent.getX(), mouseEvent.getY());
    }

    public void onCanvasDragged(MouseEvent mouseEvent) {
        if (gameManager == null) {
            return;
        }
        Vector2D dragOffset = Vector2D.create(mouseEvent.getX(), mouseEvent.getY()).subtract(dragStart);
        transformation.setOffset(dragOffset.add(offsetBackup));
        drawShapes();
    }

    public void onCanvasZoom(ScrollEvent scrollEvent) {
        if (gameManager == null) {
            return;
        }
        Vector2D mouse = new Vector2D(scrollEvent.getX(), scrollEvent.getY());
        Vector2D direction = transformation.getOffset().subtract(mouse);

        double oldScale = transformation.getScale();
        double newScale = oldScale * Math.exp(scrollEvent.getDeltaY() * 1e-2);

        if (newScale > 0) {
            transformation.setScale(newScale);
            transformation.setOffset(mouse.add(direction.multiply(newScale / oldScale)));
            drawShapes();
        }
    }
}
