package com.treasure.hunt.service.io;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImageService {
    private static ImageService instance;
    private final List<Image> cache;

    private ImageService() {
        cache = new ArrayList<>();
    }

    public synchronized static ImageService getInstance() {
        if (instance == null) {
            instance = new ImageService();
        }
        return instance;
    }

    public Image load(String toExternalForm, int maxWidth, int maxHeight, boolean preserveRatio, boolean smooth) {
        Optional<Image> cachedImage = cache.stream()
                .filter(image -> image.getUrl().equals(toExternalForm) && image.getRequestedHeight() == maxHeight && image.getRequestedWidth() == maxWidth)
                .findFirst();
        if (cachedImage.isPresent()) {
            return cachedImage.get();
        }
        Image image = new Image(toExternalForm, maxWidth, maxHeight, preserveRatio, smooth);
        cache.add(image);
        return image;
    }
}
