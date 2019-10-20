package com.treasure.hunt.ui.in_game;

import com.treasure.hunt.strategy.Generator;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryItem;
import io.reactivex.Observable;

public abstract class UiRenderer {

    public void registerStrategy(Generator strategy) {
        Observable<VisualisationGeometryItem> visualisationGeometryList = strategy.getVisualisationGeometryList();
        visualisationGeometryList.subscribe(this::addVisualisationGeometryItem);
    }

    protected abstract void addVisualisationGeometryItem(VisualisationGeometryItem visualisationGeometryItem);
}
