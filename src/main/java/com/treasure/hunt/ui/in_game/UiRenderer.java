package com.treasure.hunt.ui.in_game;

import com.treasure.hunt.strategy.Strategy;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryItem;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public abstract class UiRenderer {

    public void registerStrategy(Strategy strategy) {
        ObservableList<VisualisationGeometryItem> visualisationGeometryList = strategy.getVisualisationGeometryList();
        visualisationGeometryList.addListener((ListChangeListener<? super VisualisationGeometryItem>) c -> {
            c.getAddedSubList().forEach(this::addVisualisationGeometryItem);
            c.getRemoved().forEach(this::removeVisualisationGeometryItem);
        });
    }

    protected abstract void addVisualisationGeometryItem(VisualisationGeometryItem visualisationGeometryItem);

    protected abstract void removeVisualisationGeometryItem(VisualisationGeometryItem visualisationGeometryItem);
}
