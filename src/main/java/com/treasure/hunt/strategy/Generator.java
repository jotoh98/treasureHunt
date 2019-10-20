package com.treasure.hunt.strategy;

import com.treasure.hunt.strategy.visualisation.VisualisationGeometryItem;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class Generator {

    @Getter
    protected final List<VisualisationGeometryType> availableVisualisationGeometryTypes = new ArrayList<>();
    @Getter
    protected final ObservableList<VisualisationGeometryItem> visualisationGeometryList = FXCollections.observableArrayList();

    public abstract String getDisplayName();
}
