package com.treasure.hunt.view.widget;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.service.io.FileService;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import lombok.extern.slf4j.Slf4j;

/**
 * @author axel1200
 */
@Slf4j
public class SaveAndLoadController {
    public Button saveButton;
    private ObjectProperty<GameManager> gameManager;

    public void initialize() {
    }

    public void init(ObjectProperty<GameManager> gameManager) {
        this.gameManager = gameManager;
        saveButton.disableProperty().bind(gameManager.isNull());
    }

    public void save() {
        FileService.getInstance().saveGameManager(gameManager.get());
    }

    public void load() {
        FileService.getInstance().loadGameManager();
    }
}

