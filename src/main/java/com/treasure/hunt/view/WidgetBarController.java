package com.treasure.hunt.view;

import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
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

    private AnchorPane firstPane = new AnchorPane();
    private AnchorPane secondPane = new AnchorPane();

    private double dividerPosition;

    public void initialize() {
        firstScrollPane.setFitToWidth(true);
        firstScrollPane.setFitToHeight(true);
        firstScrollPane.setFitToWidth(true);
        firstScrollPane.setFitToHeight(true);
        firstScrollPane.setContent(firstPane);
        secondScrollPane.setContent(secondPane);
        dividerPosition = .5;

        if (widgetBar.getOrientation() == Orientation.VERTICAL) {
            firstPane.minHeightProperty().bind(firstScrollPane.heightProperty());
            secondPane.minHeightProperty().bind(secondScrollPane.heightProperty());
        } else {
            firstPane.minWidthProperty().bind(firstScrollPane.widthProperty());
            secondPane.minWidthProperty().bind(secondScrollPane.widthProperty());
        }

        init(firstScrollPane);
        init(secondScrollPane);
    }

    private void init(ScrollPane widgetWrapper) {
        if (widgetBar.getOrientation() == Orientation.HORIZONTAL) {
            widgetWrapper.setFitToHeight(true);
        } else {
            widgetWrapper.setFitToWidth(true);
        }
    }

    public void addWidget(boolean first, Region widget) {
        if (widget == null) {
            return;
        }
        AnchorPane.setTopAnchor(widget, 0d);
        AnchorPane.setRightAnchor(widget, 0d);
        AnchorPane.setBottomAnchor(widget, 0d);
        AnchorPane.setLeftAnchor(widget, 0d);
        widget.setPrefWidth(0);
        widget.setPrefHeight(0);
        (first ? firstPane : secondPane).getChildren().add(widget);
    }

    public void bindToggleGroups(ToolbarController controller) {

        controller.getFirstGroup().selectedToggleProperty().addListener((observable, oldValue, thisToggle) -> {
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
