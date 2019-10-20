package com.treasure.hunt.ui.in_game;

import com.treasure.hunt.strategy.Generator;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryItem;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public abstract class UiRenderer {

    public void registerStrategy(Generator strategy) {
        ObservableList<VisualisationGeometryItem> visualisationGeometryList = strategy.getVisualisationGeometryList();
        visualisationGeometryList.addListener((ListChangeListener<? super VisualisationGeometryItem>) change -> {
            change.getAddedSubList().forEach(this::addVisualisationGeometryItem);
            change.getRemoved().forEach(this::removeVisualisationGeometryItem);
        });
    }

    protected abstract void addVisualisationGeometryItem(VisualisationGeometryItem visualisationGeometryItem);

    protected abstract void removeVisualisationGeometryItem(VisualisationGeometryItem visualisationGeometryItem);
}
