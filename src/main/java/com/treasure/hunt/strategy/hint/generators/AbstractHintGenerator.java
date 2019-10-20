package com.treasure.hunt.strategy.hint.generators;

import com.treasure.hunt.strategy.Strategy;
import com.treasure.hunt.strategy.hint.hints.Hint;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryItem;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHintGenerator<T extends Hint> implements Strategy {
    private static VisualisationGeometryType WAY_POINT = new VisualisationGeometryType("Way points", Color.BLACK, true);
    @Getter
    protected final List<VisualisationGeometryType> availableVisualisationGeometryTypes = new ArrayList<>();
    @Getter
    private final ObservableList<VisualisationGeometryItem> visualisationGeometryList = FXCollections.observableArrayList();
    protected Point treasureLocation;
    protected double insecurity;

    protected abstract T generate(Point currentLocationOfAgent);

    public void init(Point treasureLocation, double insecurity) {
        this.treasureLocation = treasureLocation;
        this.insecurity = insecurity;
    }

    public T generateHint(Point currentLocationOfAgent) {
        VisualisationGeometryItem wayPoint = new VisualisationGeometryItem(currentLocationOfAgent, WAY_POINT);
        visualisationGeometryList.add(wayPoint);
        return generate(currentLocationOfAgent);
    }


}