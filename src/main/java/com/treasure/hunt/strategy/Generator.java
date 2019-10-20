package com.treasure.hunt.strategy;

import com.treasure.hunt.strategy.visualisation.VisualisationGeometryItem;
import com.treasure.hunt.strategy.visualisation.VisualisationGeometryType;
import io.reactivex.Observable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class Generator {

    @Getter
    protected final List<VisualisationGeometryType> availableVisualisationGeometryTypes = new ArrayList<>();
    @Getter
    protected final Observable<VisualisationGeometryItem> visualisationGeometryList = null;

    public abstract String getDisplayName();
}
