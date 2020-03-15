package com.treasure.hunt.view.widget;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jotoh
 */
@Slf4j
public class Widget<C, P extends Region> {

    @Getter
    private String resourcePath;

    @Getter
    private C controller;

    @Getter
    private P component = null;

    public Widget(String path) {
        resourcePath = path;
        init();
    }

    @SneakyThrows
    private void init() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
        component = loader.load();
        controller = loader.getController();
    }
}
