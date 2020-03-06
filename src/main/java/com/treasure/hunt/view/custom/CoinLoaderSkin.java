package com.treasure.hunt.view.custom;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CoinLoaderSkin implements Skin<CoinLoader> {
    protected CoinLoader coinLoader;
    protected List<Image> images = load();
    protected ImageView imageView;
    protected Timeline timeline = new Timeline();

    public CoinLoaderSkin(final CoinLoader coinLoader) {
        this.coinLoader = coinLoader;
        imageView = new ImageView(images.get(0));
        imageView.fitHeightProperty().bind(coinLoader.prefHeightProperty());
        imageView.fitWidthProperty().bind(coinLoader.prefWidthProperty());
        animation();
    }

    public void animation() {
        AtomicInteger i = new AtomicInteger();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().addAll(new KeyFrame(
                Duration.seconds(.1),
                event -> {
                    int index = i.get();
                    imageView.setImage(images.get(index));
                    i.set((index + 1) % images.size());
                }
        ));
        timeline.setDelay(Duration.seconds(1));
        timeline.play();
    }

    private List<Image> load() {
        return IntStream.range(0, 8)
                .mapToObj(index -> new Image(
                        CoinLoader.class.getResource(String.format("/images/coin/coin%s@3x.png", index)).toString())
                )
                .collect(Collectors.toList());
    }

    @Override
    public CoinLoader getSkinnable() {
        return coinLoader;
    }

    @Override
    public Node getNode() {
        return imageView;
    }

    @Override
    public void dispose() {
        timeline.stop();
    }
}
