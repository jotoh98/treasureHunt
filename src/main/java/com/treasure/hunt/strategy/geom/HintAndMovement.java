package com.treasure.hunt.strategy.geom;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class HintAndMovement {

    @Getter
    /**
     * Add status items that are later displayed in status widget
     */
    private List<StatusMessageItem> statusMessageItemsToBeAdded = new ArrayList<>();

    @Getter
    /**
     * Earlier added status message items that are now removed from display
     */
    private List<StatusMessageItem> statusMessageItemsToBeRemoved = new ArrayList<>();

}
