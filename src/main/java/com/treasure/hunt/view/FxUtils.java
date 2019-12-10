package com.treasure.hunt.view;

import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class FxUtils {
    public static Controller insert(String filePath) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(FxUtils::controllerFactory);
        fxmlLoader.setLocation(FXMLLoader.class.getResource(filePath));
        fxmlLoader.load();
        return fxmlLoader.getController();
    }

    private static Class controllerFactory(Class<?> param) {
        return param;
    }
}
