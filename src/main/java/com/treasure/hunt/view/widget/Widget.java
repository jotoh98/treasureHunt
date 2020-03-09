package com.treasure.hunt.view.widget;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Widget wrapper class to formalize a widget.
 *
 * @author jotoh
 */
@Slf4j
@Getter
public class Widget<C, P extends Pane> {

    /**
     * The relative path to the fxml resource.
     */
    private String resourcePath;

    /**
     * The associated controller of the widget.
     */
    private C controller;

    /**
     * The widget component box.
     */
    private P component = null;

    /**
     * Default constructor loading the properties from the resource path.
     *
     * @param path path to the fxml resource
     */
    public Widget(String path) {
        resourcePath = path;
        init();
    }

    /**
     * Load the properties from the resource path.
     */
    @SneakyThrows
    private void init() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
        component = loader.load();
        controller = loader.getController();
    }
}
