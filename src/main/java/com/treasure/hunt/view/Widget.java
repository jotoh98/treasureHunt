package com.treasure.hunt.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Widget<C, P extends Parent> {

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
