package com.treasure.hunt.view;

import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jotoh
 */
@Slf4j
public class WidgetBarController {

    @Getter
    @FXML
    private SplitPane widgetBar;

    private ScrollPane firstScrollPane = new ScrollPane();

    private ScrollPane secondScrollPane = new ScrollPane();

    private Pane firstPane = new Pane();
    private Pane secondPane = new Pane();

    private double dividerPosition;

    public void initialize() {
        firstScrollPane.setContent(firstPane);
        secondScrollPane.setContent(secondPane);
        dividerPosition = .5;
        init(firstScrollPane);
        init(secondScrollPane);
    }

    private void init(ScrollPane widgetWrapper) {
        if (widgetBar.getOrientation() == Orientation.HORIZONTAL) {
            widgetWrapper.setFitToHeight(true);
        } else {
            widgetWrapper.setFitToWidth(true);
        }
        widgetWrapper.prefWidthProperty().bind(widgetWrapper.widthProperty());
        widgetWrapper.prefHeightProperty().bind(widgetWrapper.heightProperty());
    }

    public void addWidget(boolean first, Pane widget) {
        widget.maxWidth(Double.MAX_VALUE);
        widget.maxHeight(Double.MAX_VALUE);
        widget.minWidth(Region.USE_PREF_SIZE);
        widget.minHeight(Region.USE_PREF_SIZE);
        widget.prefWidthProperty().bind((first ? firstScrollPane : secondScrollPane).widthProperty());
        (first ? firstPane : secondPane).getChildren().add(widget);
    }

    public void bindToggleGroups(ToolbarController controller) {

        controller.getFirstGroup().selectedToggleProperty().addListener((observable, oldValue, thisToggle) -> {
            log.info("{}", thisToggle);
            if (thisToggle == null) {
                if (widgetBar.getItems().size() == 2) {
                    dividerPosition = widgetBar.getDividerPositions()[0];
                }
                widgetBar.getItems().remove(firstScrollPane);
            } else if (!widgetBar.getItems().contains(firstScrollPane)) {
                widgetBar.getItems().add(0, firstScrollPane);
                if (widgetBar.getItems().size() == 2) {
                    widgetBar.setDividerPosition(0, dividerPosition);
                }
            }
        });

        controller.getSecondGroup().selectedToggleProperty().addListener((observable, oldValue, thisToggle) -> {
            if (thisToggle == null) {
                if (widgetBar.getItems().size() == 2) {
                    dividerPosition = widgetBar.getDividerPositions()[0];
                }
                widgetBar.getItems().remove(secondScrollPane);
            } else if (!widgetBar.getItems().contains(secondScrollPane)) {
                widgetBar.getItems().add(secondScrollPane);
                if (widgetBar.getItems().size() == 2) {
                    widgetBar.setDividerPosition(0, dividerPosition);
                }
            }
        });
    }
}
