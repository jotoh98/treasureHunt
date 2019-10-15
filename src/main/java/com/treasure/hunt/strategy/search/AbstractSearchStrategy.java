package com.treasure.hunt.strategy.search;

import com.treasure.hunt.strategy.Strategy;
import com.treasure.hunt.strategy.hint.hints.Hint;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryItem;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSearchStrategy<T extends Hint> implements Strategy {
    @Getter
    protected final List<VisualisationGeometryType> availableVisualisationGeometryTypes = new ArrayList<>();
    @Getter
    private final ObservableList<VisualisationGeometryItem> visualisationGeometryList = FXCollections.observableArrayList();

    public abstract void init();

    public abstract List<Point> getNextMoves(T hint, Point currentLocation);
}
