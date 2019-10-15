package com.treasure.hunt.strategy;

import com.treasure.hunt.strategy.visualisation.VisualisationGeometryItem;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryType;
import javafx.collections.ObservableList;

import java.util.List;

public interface Strategy {
    ObservableList<VisualisationGeometryItem> getVisualisationGeometryList();

    List<VisualisationGeometryType> getAvailableVisualisationGeometryTypes();

    String getDisplayName();
}
