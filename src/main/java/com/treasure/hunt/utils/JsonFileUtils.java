package com.treasure.hunt.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class JsonFileUtils {
    private static final Gson GSON;

    static {
        GSON = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .create();
        RuntimeTypeAdapterFactory<GameEngine> gameEngineAdapterFactory
                = RuntimeTypeAdapterFactory.of(GameEngine.class, "type");
        RuntimeTypeAdapterFactory<Searcher> searcherAdapterFactory
                = RuntimeTypeAdapterFactory.of(Searcher.class, "type");
        RuntimeTypeAdapterFactory<Hider> hiderAdapterFactory
                = RuntimeTypeAdapterFactory.of(Hider.class, "type");
    }

    public static void writeGameDataToFile(GameManager gameManager, Path filePath) throws IOException {
        String json = GSON.toJson(gameManager);
        Files.writeString(filePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public static GameManager readGameDataFromFile(Path filePath) throws IOException {
        String json = Files.readString(filePath);
        return GSON.fromJson(json, GameManager.class);
    }


}
